package net.hillsdon.reviki.converter;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;

import com.google.common.base.Optional;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.wiki.renderer.creole.Creole;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleTokens;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.Visitor;

/**
 * Converts Reviki to Markdown.
 *
 * May be used as many times as needed.
 *
 * @author epb
 */
public class MarkdownConverter {
  private final LinkPartsHandler _handler = new NullLinksPartHandler();
  private final Visitor _visitor = new Visitor(new PageInfoImpl(""), _handler, _handler);

  private Optional<ParseTree> tryParse(final CreoleTokens lexer, final PredictionMode pmode) {
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);
    parser.getInterpreter().setPredictionMode(pmode);
    try {
      return Optional.of((ParseTree) parser.creole());
    }
    catch (Exception e) {
      return Optional.<ParseTree> absent();
    }
  }

  /**
   * Reads and converts the contents of the given InputStream of Reviki.
   *
   * The stream is always closed, even if an Exception is thrown.
   * @param stream the stream to convert to Markdown
   * @return Markdown representation of the given Reviki
   * @throws IOException if reading the given stream fails
   */
  public String convert(final InputStream stream) throws IOException {
    try {
      CreoleTokens lexer = new CreoleTokens(null);
      lexer.setInputStream(new ANTLRInputStream(stream));

      // First try parsing in SLL mode. This is really fast for pages with no
      // parse errors.
      Optional<ParseTree> tree = tryParse(lexer, PredictionMode.SLL)
          .or(tryParse(lexer, PredictionMode.LL));
      if (!tree.isPresent()) {
        throw new RuntimeException("Failed to parse given reviki");
      }
      return new MarkdownCreoleRenderer().visit(_visitor.visit(tree.get()));
      }
    finally {
      stream.close();
    }
  }

}
