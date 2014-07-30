package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.hillsdon.reviki.web.urls.URLOutputFilter;

import com.google.common.base.Optional;

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

  public ASTRenderer(T nullval) {
    _enabledDirectives = new HashMap<String, List<String>>();
    _nullval = nullval;
  }

  /**
   * Default constructor for cases where `null` is the null value.
   */
  public ASTRenderer() {
    this(null);
  }

  /**
   * Set the URL output filter.
   */
  public void setUrlOutputFilter(URLOutputFilter urlOutputFilter) {
    _urlOutputFilter = urlOutputFilter;
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
  protected void enable(String directive, List<String> args) {
    _enabledDirectives.put(directive, args);
  }

  /**
   * Turn off a directive.
   */
  protected void disable(String directive) {
    _enabledDirectives.remove(directive);
  }

  /**
   * Check if a directive is enabled.
   */
  protected boolean isEnabled(String directive) {
    return _enabledDirectives.containsKey(directive);
  }

  /**
   * Get the args for a directive, if enabled.
   */
  protected Optional<List<String>> getArgs(String directive) {
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
  protected List<String> unsafeGetArgs(String directive) {
    return getArgs(directive).get();
  }

  /**
   * Default behaviour for visiting ASTNodes.
   */
  @Override
  public T visitASTNode(ASTNode node) {
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
  public T visitDirectiveNode(DirectiveNode node) {
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
  protected T combine(T x1, T x2) {
    return (x1 == _nullval) ? x2 : x1;
  }
}
