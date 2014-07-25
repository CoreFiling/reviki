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
   * How deep macros will be expanded
   */
  public static final int MACRO_DEPTH_LIMIT = 100;

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
   * @param pmode The prediction mode.
   * @return A parse tree.
   */
  private static Optional<ParseTree> tryParse(final CommonTokenStream tokens, final Creole parser, final PredictionMode pmode) {
    parser.getInterpreter().setPredictionMode(pmode);

    try {
      return Optional.of((ParseTree) parser.creole());
    }
    catch (Exception e) {
      tokens.reset();
      parser.reset();

      return Optional.<ParseTree> absent();
    }
  }

  /**
   * Render a stream of text.
   *
   * @param in The input stream to render.
   * @param visitor The visitor to do the rendering.
   * @param macros List of macros to apply.
   * @param reset Whether to reset the expansion limit or not.
   * @return The AST of the page, after macro expansion.
   */
  private static ASTNode renderInternal(final ANTLRInputStream in, final CreoleASTBuilder visitor, final Supplier<List<Macro>> macros, final boolean reset) {
    CreoleTokens lexer = new CreoleTokens(in);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    // First try parsing in SLL mode. This is really fast for pages with no
    // parse errors.
    Optional<ParseTree> tree = tryParse(tokens, parser, PredictionMode.SLL);

    if (!tree.isPresent()) {
      tree = tryParse(tokens, parser, PredictionMode.LL);
    }

    ASTNode rendered = visitor.visit(tree.get());

    // Expand macros
    if (reset) {
      _expansionLimit = MACRO_DEPTH_LIMIT;
    }

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
  public static ASTNode render(final PageStore store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    CreoleASTBuilder visitor = new Visitor(store, page, urlOutputFilter, linkHandler, imageHandler);
    return renderWithVisitor(visitor, macros);
  }

  /** Render a page with no store. */
  public static ASTNode render(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    CreoleASTBuilder visitor = new Visitor(page, urlOutputFilter, linkHandler, imageHandler);
    return renderWithVisitor(visitor, macros);
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
   * Render a wiki page.
   *
   * @param visitor The AST builder.
   * @param macros List of macros to reply.
   * @return The AST of the page, after macro application.
   */
  public static ASTNode renderWithVisitor(final CreoleASTBuilder visitor, final Supplier<List<Macro>> macros) {
    String contents = visitor.page().getContent();

    // The grammar and lexer assume they'll not hit an EOF after various things,
    // so add a newline in if there's not one already there.
    if (contents.length() == 0 || !contents.substring(contents.length() - 1).equals("\n")) {
      contents += "\n";
    }

    return renderInternal(new ANTLRInputStream(contents), visitor, macros, true);
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
   * @param visitor The AST builder.
   * @param macros List of macros to apply.
   * @return The AST of the page, after macro expansion.
   */
  public static ASTNode renderPartWithVisitor(final String content, final CreoleASTBuilder visitor, final Supplier<List<Macro>> macros) {
    return renderInternal(new ANTLRInputStream(content), visitor, macros, false);
  }
}
