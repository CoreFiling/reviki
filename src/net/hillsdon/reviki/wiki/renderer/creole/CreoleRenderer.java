package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.links.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Renderer for the Reviki dialect of WikiCreole.
 *
 * @author msw
 */
public class CreoleRenderer {
  /**
   * Macro expansion depth limit. This gets reset when rendering a page, but not
   * when rendering a partial page. If it hits zero, no macros are expanded.
   */
  private static int _expansionLimit;

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
  private static ParseTree tryParse(final CommonTokenStream tokens, final Creole parser, final ANTLRErrorStrategy errors, final PredictionMode pmode) {
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
   * Helper for tryParse using the default error strategy. See
   * {@link #tryParse(CommonTokenStream, Creole, ANTLRErrorStrategy, PredictionMode)}
   */
  private static ParseTree tryParse(final CommonTokenStream tokens, final Creole parser, final PredictionMode pmode) {
    return tryParse(tokens, parser, new DefaultErrorStrategy(), pmode);
  }

  /**
   * Render a stream of text.
   *
   * @param in The input stream to render.
   * @param visitor The visitor to do the rendering.
   * @param macros List of macros to apply.
   * @return The AST of the page, after macro expansion.
   */
  private static ASTNode renderInternal(final ANTLRInputStream in, final CreoleASTBuilder visitor, final Supplier<List<Macro>> macros) {
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
      tree = tryParse(tokens, parser, PredictionMode.LL);
    }

    ASTNode rendered = visitor.visit(tree);

    // Expand macros
    ASTNode expanded = rendered;
    _expansionLimit--;

    if (_expansionLimit >= 0) {
      expanded = rendered.expandMacros(macros);
    }

    _expansionLimit++;
    return expanded;
  }

  /**
   * Render a wiki page.
   *
   * @param store The page store.
   * @param page The page to render.
   * @param urlOutputFilter Filter to apply to URLs (handling jsessionid values
   *          etc).
   * @param linkHandler Handler for resolving and rendering links
   * @param imageHandler Handler for resolving and rendering images
   * @param macros List of macros to reply
   * @return The AST of the page, after macro application.
   */
  public static ASTNode render(final Optional<PageStore> store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    String contents = page.getContent();

    // The grammar and lexer assume they'll not hit an EOF after various things,
    // so add a newline in if there's not one already there.
    if (contents.length() == 0 || !contents.substring(contents.length() - 1).equals("\n")) {
      contents += "\n";
    }

    // Reset the expansion limit.
    _expansionLimit = 100;

    CreoleASTBuilder visitor = new Visitor(store, page, urlOutputFilter, linkHandler, imageHandler);
    return renderInternal(new ANTLRInputStream(contents), visitor, macros);
  }

  /** Render a page with a store. */
  public static ASTNode render(final PageStore store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    return render(Optional.of(store), page, urlOutputFilter, linkHandler, imageHandler, macros);
  }

  /** Render a page with no store. */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    return render(Optional.<PageStore> absent(), page, urlOutputFilter, linkHandler, imageHandler, macros);
  }

  /** Render a page with no macros. */
  public static ASTNode render(final PageStore store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    Supplier<List<Macro>> macros = Suppliers.ofInstance((List<Macro>) new ArrayList<Macro>());
    return render(store, page, urlOutputFilter, linkHandler, imageHandler, macros);
  }

  /** Render a page with no macros or store */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    Supplier<List<Macro>> macros = Suppliers.ofInstance((List<Macro>) new ArrayList<Macro>());
    return render(page, urlOutputFilter, linkHandler, imageHandler, macros);
  }

  /**
   * Render only a part of a page.
   *
   * @param store The page store (may be null).
   * @param page The containing page.
   * @param content The content to render.
   * @param urlOutputFilter Filter to apply to URLs (handling jsessionid values
   *          etc).
   * @param linkHandler Handler for resolving and rendering links
   * @param imageHandler Handler for resolving and rendering images
   * @param macros List of macros to apply
   * @return The AST of the page, after macro application.
   */
  public static ASTNode renderPart(final PageStore store, final PageInfo page, final String content, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    CreoleASTBuilder visitor = new Visitor(store, page, urlOutputFilter, linkHandler, imageHandler);
    return renderPartWithVisitor(content, visitor, macros);
  }

  /**
   * Render only a part of a page.
   *
   * @param content The content to render.
   * @param visitor The AST builder (may not be null)
   * @param macros List of macros to apply
   * @return The AST of the page, after macro expansion.
   */
  public static ASTNode renderPartWithVisitor(final String content, final CreoleASTBuilder visitor, final Supplier<List<Macro>> macros) {
    return renderInternal(new ANTLRInputStream(content), visitor, macros);
  }
}
