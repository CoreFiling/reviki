package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

import com.google.common.base.Optional;

/**
 * Wrapper over ASTVisitor to make rendering easier. In particular, this tracks
 * enabled directives, and provides a monoid-like abstraction over the output.
 *
 * @author msw
 */
public abstract class ASTRenderer<T> extends ASTVisitor<T> {
  /** Directives (and arguments) active at this time. */
  private final Map<String, List<String>> _enabledDirectives;

  /** Apply final touches (jsessionid params, etc) to URLs. */
  private URLOutputFilter _urlOutputFilter;

  /**
   * A null value for the output type. This is the unit of
   * {@link #combine(T, T)}.
   */
  private final T _nullval;

  public ASTRenderer(final T nullval, final URLOutputFilter urlOutputFilter) {
    _enabledDirectives = new HashMap<String, List<String>>();
    _nullval = nullval;
    _urlOutputFilter = urlOutputFilter;
  }

  /**
   * Shorthand constructor for cases where `null` is the null value.
   */
  public ASTRenderer(final URLOutputFilter urlOutputFilter) {
    this(null, urlOutputFilter);
  }

  /**
   * Get the URL output filter.
   */
  protected URLOutputFilter urlOutputFilter() {
    return _urlOutputFilter;
  }

  /**
   * Get the null value.
   */
  protected T nullval() {
    return _nullval;
  }

  /**
   * Turn on a directive. If the directive was already enabled, replace its
   * arguments.
   */
  protected void enable(final String directive, final List<String> args) {
    _enabledDirectives.put(directive, args);
  }

  /**
   * Turn off a directive.
   */
  protected void disable(final String directive) {
    _enabledDirectives.remove(directive);
  }

  /**
   * Check if a directive is enabled.
   */
  protected boolean isEnabled(final String directive) {
    return _enabledDirectives.containsKey(directive);
  }

  /**
   * Get the args for a directive, if enabled.
   */
  protected Optional<List<String>> getArgs(final String directive) {
    if (isEnabled(directive)) {
      return Optional.of(_enabledDirectives.get(directive));
    }
    else {
      return Optional.<List<String>> absent();
    }
  }

  /**
   * Get the args for a directive, unsafely.
   */
  protected List<String> unsafeGetArgs(final String directive) {
    return getArgs(directive).get();
  }

  /**
   * Default behaviour for visiting ASTNodes.
   */
  @Override
  public T visitASTNode(final ASTNode node) {
    T out = _nullval;

    for (ASTNode child : node.getChildren()) {
      out = combine(out, visit(child));
    }

    return out;
  }

  /**
   * Default behaviour for visiting DirectiveNodes.
   */
  @Override
  public T visitDirectiveNode(final DirectiveNode node) {
    if (node.isEnabled()) {
      // If a directive is enabled multiple times, the most recent one takes
      // effect. This is because the arguments may be different.
      enable(node.getName(), node.getArgs());
    }
    else {
      disable(node.getName());
    }

    return _nullval;
  }

  /**
   * Default behaviour for visiting Images: expand the target and call either a
   * display function or an error handler.
   */
  @Override
  public T visitImage(final Image node) {
    LinkPartsHandler handler = node.getHandler();
    PageInfo page = node.getPage();
    LinkParts parts = node.getParts();

    try {
      return renderImage(handler.handle(page, parts, urlOutputFilter()), node.getTitle(), node);
    }
    catch (Exception e) {
      return renderBrokenImage(node);
    }
  }

  /**
   * Render an image. The default implementation does nothing, allowing people
   * to choose instead to override visitLink.
   */
  public T renderImage(final String target, final String title, final Image node) {
    return nullval();
  }

  /**
   * Render a broken image. The default implementation simply displays it as
   * text.
   */
  public T renderBrokenImage(final Image node) {
    String title = node.getTitle();
    String target = node.getTarget();
    String imageText;
    if (title.equals(target)) {
      imageText = "{{" + target + "}}";
    }
    else {
      imageText = "{{" + target + "|" + title + "}}";
    }

    System.err.println("Failed to insert image " + imageText);

    return visitPlaintext(new Plaintext(imageText));
  }

  /**
   * Default behaviour for visiting Links: expand the target and call either a
   * display function or an error handler.
   */
  @Override
  public T visitLink(final Link node) {
    LinkPartsHandler handler = node.getHandler();
    PageInfo page = node.getPage();
    LinkParts parts = node.getParts();

    String title = node.getTitle();
    String target = node.getTarget();

    // Yay, special cases!
    if (handler.isAcronymNotLink(parts)) {
      return visitPlaintext(new Plaintext(target));
    }

    try {
      return renderLink(handler.handle(page, parts, urlOutputFilter()), title, node);
    }
    catch (Exception e) {
      // Treat mailto links specially.
      if (target.startsWith("mailto:")) {
        return renderLink(target, title, node);
      }
      else {
        return renderBrokenLink(node);
      }
    }
  }

  /**
   * Render a link. The default implementation does nothing, allowing people to
   * choose instead to override visitLink.
   */
  public T renderLink(final String target, final String title, final Link node) {
    return nullval();
  }

  /**
   * Render a broken link. The default implementation simply displays it as
   * text.
   */
  public T renderBrokenLink(final Link node) {
    // Just display the link as text.
    String title = node.getTitle();
    String target = node.getTarget();
    String linkText;
    if (title.equals(target)) {
      linkText = "[[" + target + "]]";
    }
    else {
      linkText = "[[" + target + "|" + title + "]]";
    }

    System.err.println("Failed to insert link " + linkText);

    return visitPlaintext(new Plaintext(linkText));
  }

  /**
   * Combine two values. Together with {@link #nullval()}, this should form a
   * monoid:
   *
   * combine(nullval(), x) = x = combine(x, nullval()).
   *
   * combine(a, combine(b, c)) = combine(combine(a, b), c).
   *
   * The default implementation returns the leftmost non-nullval() (compared
   * with ==) value.
   */
  protected T combine(final T x1, final T x2) {
    return (x1 == _nullval) ? x2 : x1;
  }
}
