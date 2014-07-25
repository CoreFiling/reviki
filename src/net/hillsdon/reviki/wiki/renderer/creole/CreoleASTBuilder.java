package net.hillsdon.reviki.wiki.renderer.creole;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.Creole.InlineContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Code;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Inline;
import net.hillsdon.reviki.wiki.renderer.creole.ast.InlineCode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ListItem;
import net.hillsdon.reviki.wiki.renderer.creole.ast.OrderedList;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Plaintext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.UnorderedList;

/**
 * Helper class providing some useful functions for walking through the Creole
 * parse tree and building an AST.
 *
 * @author msw
 */
public abstract class CreoleASTBuilder extends CreoleBaseVisitor<ASTNode> {
  /** The page store (may be null). */
  private final PageStore _store;

  /** The page being rendered. */
  private final PageInfo _page;

  /** The URL handler for links. */
  private final LinkPartsHandler _linkHandler;

  /** The URL handler for images. */
  private final LinkPartsHandler _imageHandler;

  /** A final pass over URLs to apply any last-minute changes. */
  private final URLOutputFilter _urlOutputFilter;

  public PageStore store() {
    return _store;
  }

  public PageInfo page() {
    return _page;
  }

  public LinkPartsHandler linkHandler() {
    return _linkHandler;
  }

  public LinkPartsHandler imageHandler() {
    return _imageHandler;
  }

  public URLOutputFilter urlOutputFilter() {
    return _urlOutputFilter;
  }

  /**
   * Aggregate results produced by visitChildren. This returns the "right"most
   * non-null result found.
   */
  @Override
  protected ASTNode aggregateResult(final ASTNode aggregate, final ASTNode nextResult) {
    if (nextResult == null) {
      return aggregate;
    }
    else {
      return nextResult;
    }
  }

  /**
   * Construct a new parse tree visitor.
   *
   * @param store The page store (may be null).
   * @param page The page being rendered.
   * @param urlOutputFilter The URL post-render processor.
   * @param handler The URL renderer
   */
  public CreoleASTBuilder(final PageStore store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    _store = store;
    _page = page;
    _urlOutputFilter = urlOutputFilter;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
  }

  /**
   * Render some inline markup (like bold, italic, or strikethrough) by
   * converting the opening symbol into a plaintext AST node if the closing
   * symbol was missing.
   *
   * If italics is "//", this lets us render things like "//foo bar" as
   * "//foo bar" rather than, as ANTLR's error recovery would produce, "
   * <em>foo bar</em>".
   *
   * @param symbol The opening/closing symbol.
   * @param sname The name of the ending symbol
   * @param type The AST node class to use on success
   * @param end The (possibly missing) ending token.
   * @param inline The contents of the markup
   * @return Either an instance of the marked-up node if the ending token is
   *         present, or an inline node consisting of a plaintext start token
   *         followed by the rest of the rendered content.
   */
  protected ASTNode renderInlineMarkup(final Class<? extends ASTNode> type, final String symbol, final String sname, final TerminalNode end, final InlineContext inline) {
    ASTNode inner = (inline != null) ? visit(inline) : new Plaintext("");

    // If the end tag is missing, undo the error recovery
    if (end == null || ("<missing " + sname + ">").equals(end.getText())) {
      List<ASTNode> chunks = new ArrayList<ASTNode>();
      chunks.add(new Plaintext(symbol));
      chunks.add(inner);
      return new Inline(chunks);
    }

    // If the inner text is missing, this is not markup
    if (inner.toXHTML().trim().equals("")) {
      List<ASTNode> chunks = new ArrayList<ASTNode>();
      chunks.add(new Plaintext(symbol + symbol));
      chunks.add(inner);
      return new Inline(chunks);
    }

    try {
      @SuppressWarnings("unchecked")
      Constructor<ASTNode> constructor = (Constructor<ASTNode>) type.getConstructors()[0];
      return constructor.newInstance(inner);
    }
    catch (Throwable e) {
      // Never reached if you pass in correct params
      return null;
    }
  }

  protected String cutOffEndTag(final TerminalNode node, final String end) {
    String res = node.getText().replaceAll("\\s+$", "");
    return res.substring(0, res.length() - end.length());
  }

  /**
   * Render an inline piece of code with syntax highlighting.
   *
   * @param code The token containing the code
   * @param end The end marker
   * @param renderer The renderer to use
   * @return An inline code node with the end token stripped.
   */
  protected ASTNode renderInlineCode(final TerminalNode node, final String end, final Renderer renderer) {
    String code = cutOffEndTag(node, end);

    try {
      return new InlineCode(code, renderer);
    }
    catch (IOException e) {
      return new InlineCode(code);
    }
  }

  /**
   * Render an inline piece of code with syntax highlighting. See
   * {@link #renderInlineCode}.
   *
   * @param language The language to use
   */
  protected ASTNode renderInlineCode(final TerminalNode node, final String end, final String language) {
    return renderInlineCode(node, end, XhtmlRendererFactory.getRenderer(language));
  }

  /**
   * Render some inline code without syntax highlighting.
   */
  protected ASTNode renderInlineCode(final TerminalNode node, final String end) {
    return new InlineCode(cutOffEndTag(node, end));
  }

  /**
   * Render a block of code with syntax highlighting. See
   * {@link #renderInlineCode}.
   *
   * @param code The token containing the code
   * @param end The end marker
   * @param renderer The renderer to use
   * @return A block code node with the end token stripped.
   */
  protected ASTNode renderBlockCode(final TerminalNode node, final String end, final Renderer renderer) {
    String code = node.getText();
    code = code.substring(0, code.length() - end.length());

    try {
      return new Code(code, renderer);
    }
    catch (IOException e) {
      return new Code(code);
    }
  }

  /**
   * Render a block of code with syntax highlighting. See
   * {@link #renderBlockCode}.
   *
   * @param language The language to use
   */
  protected ASTNode renderBlockCode(final TerminalNode node, final String end, final String language) {
    return renderBlockCode(node, end, XhtmlRendererFactory.getRenderer(language));
  }

  /**
   * Render a block of code without syntax highlighting.
   */
  protected ASTNode renderBlockCode(final TerminalNode node, final String end) {
    return new Code(cutOffEndTag(node, end));
  }

  /**
   * Types of lists which can be constructed by renderList.
   */
  protected enum ListType {
    Ordered, Unordered
  };

  /**
   * Class to hold the context of a list item to render.
   */
  protected class ListItemContext {
    private final ListType _type;

    private final ParserRuleContext _ordered;

    private final ParserRuleContext _unordered;

    public ListItemContext(final ListType type, final ParserRuleContext ordered, final ParserRuleContext unordered) {
      this._type = type;
      this._ordered = ordered;
      this._unordered = unordered;
    }

    public ParserRuleContext get() {
      return (_type == ListType.Ordered) ? _ordered : _unordered;
    }
  }

  /**
   * Render a list.
   *
   * @param type The type of list to build.
   * @param childContexts the children of the list.
   * @return A list node containing the given elements
   */
  protected ASTNode renderList(final ListType type, final List<? extends ParserRuleContext> childContexts) {
    List<ASTNode> children = new ArrayList<ASTNode>();

    for (ParserRuleContext ctx : childContexts) {
      children.add(visit(ctx));
    }

    if (type == ListType.Ordered) {
      return new OrderedList(children);
    }
    else {
      return new UnorderedList(children);
    }
  }

  /**
   * Render a list item.
   *
   * @param childContexts List of child elements
   * @param inner Sorted list (most preferable first) of possible inner elements
   * @return A list item containing with the given child list elements. For
   *         inner elements, olist is preferred over ulist, which is preferred
   *         over inline; if none are given, an empty Plaintext is used.
   */
  protected ASTNode renderListItem(final List<ListItemContext> childContexts, final List<? extends ParserRuleContext> inner) {
    ASTNode body = new Plaintext("");

    for (ParserRuleContext in : inner) {
      if (in != null) {
        body = visit(in);
        break;
      }
    }

    if (childContexts == null || childContexts.isEmpty()) {
      return new ListItem(body);
    }
    else {
      // In general, a list can contain any arbitrary combination of ordered and
      // unordered sublists, and we want to preserve the (un)orderedness in the
      // bullet points, so we have to render them all individually.
      List<ASTNode> parts = new ArrayList<ASTNode>();
      parts.add(body);
      ListType type = null;
      List<ParserRuleContext> contexts = new ArrayList<ParserRuleContext>();

      // Build lists of (un)ordered chunks one at a time, rendering them, and
      // adding to the list of lists.
      for (ListItemContext child : childContexts) {
        if (type != null && child._type != type) {
          parts.add(renderList(type, contexts));
          contexts.clear();
        }

        type = child._type;
        contexts.add(child.get());
      }

      if (!contexts.isEmpty()) {
        parts.add(renderList(type, contexts));
      }

      return new ListItem(parts);
    }
  }
}
