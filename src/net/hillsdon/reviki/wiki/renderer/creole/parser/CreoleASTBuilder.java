package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.parser.Creole.*;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.*;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

/**
 * Helper class providing some useful functions for walking through the Creole
 * parse tree and building an AST.
 *
 * @author msw
 */
public abstract class CreoleASTBuilder extends CreoleBaseVisitor<ResultNode> {
  /** The page being rendered */
  protected PageInfo page;

  /** The URL renderer */
  protected LinkPartsHandler handler;

  /** A final pass over URLs to apply any last-minute changes */
  protected URLOutputFilter urlOutputFilter;

  /**
   * Construct a new parse tree visitor.
   *
   * @param page The page being rendered.
   * @param urlOutputFilter The URL post-render processor.
   * @param handler The URL renderer
   */
  public CreoleASTBuilder(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    this.page = page;
    this.urlOutputFilter = urlOutputFilter;
    this.handler = handler;
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
  protected ResultNode renderInlineMarkup(Class<? extends ResultNode> type, String symbol, String sname, TerminalNode end, InlineContext inline) {
    ResultNode inner = (inline != null) ? visit(inline) : new Plaintext("");

    // If the end tag is missing, undo the error recovery
    if (end == null || ("<missing " + sname + ">").equals(end.getText())) {
      List<ResultNode> chunks = new ArrayList<ResultNode>();
      chunks.add(new Plaintext(symbol));
      chunks.add(inner);
      return new Inline(chunks);
    }

    try {
      @SuppressWarnings("unchecked")
      Constructor<ResultNode> constructor = (Constructor<ResultNode>) type.getConstructors()[0];
      return constructor.newInstance(inner);
    }
    catch (Throwable e) {
      // Never reached if you pass in correct params
      return null;
    }
  }

  /**
   * Render an inline piece of code with syntax highlighting.
   * 
   * @param code The token containing the code
   * @param end The end marker
   * @param renderer The renderer to use
   * @return An inline code node with the end token stripped.
   */
  protected ResultNode renderInlineCode(TerminalNode node, String end, Renderer renderer) {
    String code = node.getText();
    return new InlineCode(code.substring(0, code.length() - end.length()), renderer);
  }

  /**
   * Render an inline piece of code with syntax highlighting. See
   * {@link #renderInlineCode}.
   * 
   * @param language The language to use
   */
  protected ResultNode renderInlineCode(TerminalNode node, String end, String language) {
    return renderInlineCode(node, end, XhtmlRendererFactory.getRenderer(language));
  }

  /**
   * Render some inline code without syntax highlighting. See
   * {@link #renderInlineCode}.
   */
  protected ResultNode renderInlineCode(TerminalNode node, String end) {
    return renderInlineCode(node, end, (Renderer) null);
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
  protected ResultNode renderBlockCode(TerminalNode node, String end, Renderer renderer) {
    String code = node.getText();
    return new Code(code.substring(0, code.length() - end.length()), renderer);
  }

  /**
   * Render a block of code with syntax highlighting. See
   * {@link #renderBlockCode}.
   * 
   * @param language The language to use
   */
  protected ResultNode renderBlockCode(TerminalNode node, String end, String language) {
    return renderBlockCode(node, end, XhtmlRendererFactory.getRenderer(language));
  }

  /**
   * Render a block of code without syntax highlighting. See
   * {@link #renderBlockCode}.
   */
  protected ResultNode renderBlockCode(TerminalNode node, String end) {
    return renderBlockCode(node, end, (Renderer) null);
  }

  /**
   * Render a list.
   * 
   * @param type The type of list to build.
   * @param childContexts List of child element contexts.
   * @param innerOlist Possible inner ordered list.
   * @param innerUlist Possible inner unordered list.
   * @param inner Possible inner inline.
   * @return An ordered list node containing the given values. For inner
   *         elements, olist is preferred over ulist, which is preferred over
   *         inner. If all are null, an empty Plaintext is used.
   */
  protected ResultNode renderList(Class<? extends ResultNode> type, List<? extends ParserRuleContext> childContexts, OlistContext innerOlist, UlistContext innerUlist, InlineContext inner) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (ParserRuleContext ctx : childContexts) {
      children.add(visit(ctx));
    }

    try {
      @SuppressWarnings("unchecked")
      Constructor<ResultNode> constructor = (Constructor<ResultNode>) type.getConstructors()[0];

      if (innerOlist != null)
        return constructor.newInstance(visit(innerOlist), children);

      if (innerUlist != null)
        return constructor.newInstance(visit(innerUlist), children);

      if (inner != null)
        return constructor.newInstance(visit(inner), children);

      return constructor.newInstance(new Plaintext(""), children);
    }
    catch (Throwable e) {
      // Never reached if you pass in correct params
      return null;
    }
  }
}
