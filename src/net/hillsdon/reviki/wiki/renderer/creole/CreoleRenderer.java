package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class CreoleRenderer {

  /**
   * Macro expansion depth limit. This gets reset when rendering a page, but not
   * when rendering a partial page. If it hits zero, no macros are expanded.
   */
  private static int expansionLimit;

  /**
   * Render a stream of text. See
   * {@link #render(PageInfo, URLOutputFilter, LinkPartsHandler)} and
   * {@link #renderPart(PageInfo, String, URLOutputFilter, LinkPartsHandler, LinkPartsHandler, List)}
   * for explanation of most arguments.
   *
   * @param in The input stream to render.
   * @return The AST of the page, after macro application.
   */
  private static ASTNode renderInternal(ANTLRInputStream in, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final List<Macro> macros) {
    CreoleTokens lexer = new CreoleTokens(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    ParseTree tree = parser.creole();

    ParseTreeVisitor<ASTNode> visitor = new Visitor(page, urlOutputFilter, linkHandler, imageHandler);

    ASTNode rendered = visitor.visit(tree);

    // Decrement the expansion limit
    expansionLimit--;

    if (expansionLimit >= 0) {
      return rendered.expandMacros(macros);
    }
    else {
      // Depth limit has been hit, there is probably a macro loop happening.
      return rendered;
    }
  }

  /**
   * Render a wiki page.
   *
   * @param page The page to render.
   * @param urlOutputFilter Filter to apply to URLs (handling jsessionid values
   *          etc).
   * @param linkHandler Handler for resolving and rendering links
   * @param imageHandler Handler for resolving and rendering images
   * @param macros List of macros to reply
   * @return The AST of the page, after macro application.
   */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final List<Macro> macros) {
    String contents = page.getContent();

    // The grammar and lexer assume they'll not hit an EOF after various things,
    // so add a newline in if there's not one already there.
    if (contents.length() == 0 || !contents.substring(contents.length() - 1).equals("\n"))
      contents += "\n";

    // Reset the expansion limit.
    expansionLimit = 100;

    return renderInternal(new ANTLRInputStream(contents), page, urlOutputFilter, linkHandler, imageHandler, macros);
  }

  /**
   * Render a page with no macros. See
   * {@link #render(PageInfo, URLOutputFilter, LinkPartsHandler)}.
   */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    return render(page, urlOutputFilter, linkHandler, imageHandler, new ArrayList<Macro>());
  }

  /**
   * Render a page with images rendered as links to their source. See
   * {@link #render(PageInfo, URLOutputFilter, LinkPartsHandler)}.
   */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final List<Macro> macros) {
    return render(page, urlOutputFilter, linkHandler, linkHandler, macros);
  }

  /**
   * Render a page with no macros, and images rendered as links to their source.
   * See {@link #render(PageInfo, URLOutputFilter, LinkPartsHandler)}.
   */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler) {
    return render(page, urlOutputFilter, linkHandler);
  }

  /**
   * Render only a part of a page.
   *
   * @param page The containing page.
   * @param content The content to render.
   * @param urlOutputFilter Filter to apply to URLs (handling jsessionid values
   *          etc).
   * @param linkHandler Handler for resolving and rendering links
   * @param imageHandler Handler for resolving and rendering images
   * @param macros List of macros to reply
   * @return The AST of the page, after macro application.
   */
  public static ASTNode renderPart(PageInfo page, String content, URLOutputFilter urlOutputFilter, LinkPartsHandler linkHandler, LinkPartsHandler imageHandler, List<Macro> macros) {
    return renderInternal(new ANTLRInputStream(content), page, urlOutputFilter, imageHandler, imageHandler, macros);
  }
}
