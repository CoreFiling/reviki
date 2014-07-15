package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.*;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class CreoleRenderer {

  /**
   * Format string for rendering bugtracker links.
   */
  public final static String BUG_LINK_URL = "https://bugs.int.corefiling.com/show_bug.cgi?id=%s";

  /**
   * Macro expansion depth limit. This gets reset when rendering a page, but not
   * when rendering a partial page. If it hits zero, no macros are expanded.
   */
  private static int expansionLimit;

  /**
   * Try to run a parser, resetting the input on failure.
   *
   * @param tokens The token stream. Consumed by the parser, and reset on
   *          failure.
   * @param parser The parser. Reset on failure.
   * @param errors The error recovery strategy.
   * @param pmode The prediction mode.
   * @return A parse tree.
   */
  private static ParseTree tryParse(CommonTokenStream tokens, Creole parser, ANTLRErrorStrategy errors, PredictionMode pmode) {
    parser.setErrorHandler(errors);
    parser.getInterpreter().setPredictionMode(pmode);
    try {
      return parser.creole();
    }
    catch (RuntimeException ex) {
      tokens.reset();
      parser.reset();
      throw ex;
    }
  }

  /**
   * Helper for tryParse using LL(*) prediction. See
   * {@link #tryParse(CommonTokenStream, Creole, ANTLRErrorStrategy, PredictionMode)}
   */
  private static ParseTree tryParse(CommonTokenStream tokens, Creole parser, ANTLRErrorStrategy errors) {
    return tryParse(tokens, parser, errors, PredictionMode.LL);
  }

  /**
   * Helper for tryParse using the default error strategy. See
   * {@link #tryParse(CommonTokenStream, Creole, ANTLRErrorStrategy, PredictionMode)}
   */
  private static ParseTree tryParse(CommonTokenStream tokens, Creole parser, PredictionMode pmode) {
    return tryParse(tokens, parser, new DefaultErrorStrategy(), pmode);
  }

  /**
   * Helper for tryParse using the default error strategy and LL(*) prediction.
   * See
   * {@link #tryParse(CommonTokenStream, Creole, ANTLRErrorStrategy, PredictionMode)}
   */
  private static ParseTree tryParse(CommonTokenStream tokens, Creole parser) {
    return tryParse(tokens, parser, new DefaultErrorStrategy(), PredictionMode.LL);
  }

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

    // First try parsing in SLL mode. This is really fast for pages with no
    // parse errors.
    ParseTree tree;

    try {
      tree = tryParse(tokens, parser, PredictionMode.SLL);
    }
    catch (Exception e1) {
      tree = tryParse(tokens, parser);
    }

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

    long startTime = System.nanoTime();
    ASTNode out = renderInternal(new ANTLRInputStream(contents), page, urlOutputFilter, linkHandler, imageHandler, macros);
    System.out.println("Rendered " + page.getPath() + " in " + (System.nanoTime() - startTime) / 1000000000.0 + "s");
    return out;
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
    return renderInternal(new ANTLRInputStream(content), page, urlOutputFilter, linkHandler, imageHandler, macros);
  }
}
