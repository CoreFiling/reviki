package net.hillsdon.reviki.converter;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.vc.impl.DummyPageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.wiki.renderer.creole.Creole;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleTokens;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleAnchors;
import net.hillsdon.reviki.wiki.renderer.creole.Visitor;

/**
 * Converts Reviki to Markdown.
 *
 * May be used as many times as needed.
 *
 * @author epb
 */
public class MarkdownConverter {
  private final InterWikiLinker _wikilinker = new InterWikiLinker();
  private final InternalLinker _linker = new JiraInternalLinker();
  private final SimplePageStore _pageStore = new DummyPageStore();

  private final Configuration _configuration = new Configuration() {
    @Override
    public InterWikiLinker getInterWikiLinker() throws PageStoreException {
      return _wikilinker;
    }
  };

  private final LinkResolutionContext _lrc = new LinkResolutionContext(_linker, _wikilinker, _configuration, _pageStore);

  private final LinkPartsHandler _handler = new JiraLinkHandler(SimpleAnchors.ANCHOR, _lrc);

  private final Visitor _visitor = new Visitor(new PageInfoImpl(""), _handler, _handler);

  public MarkdownConverter() {
    try {
      for (String linkDefinition : IOUtils.readLines(Resources.getResource(getClass(), "interwiki-links.txt").openStream())) {
        String[] parts = linkDefinition.split("\t", 2);
        if (parts.length == 2) {
          _wikilinker.addWiki(parts[0], parts[1]);
        }
      }
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to read interwiki-links configuration.");
    }
  }

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
   * @param stream the reviki string to convert to Markdown
   * @return Markdown representation of the given Reviki
   * @throws IOException if reading the given stream fails
   */
  public String convert(final String reviki) throws IOException {
    CreoleTokens lexer = new CreoleTokens(null, true);
    lexer.setInputStream(new ANTLRInputStream(reviki + "\n"));

    // First try parsing in SLL mode. This is really fast for pages with no
    // parse errors.
    Optional<ParseTree> tree = tryParse(lexer, PredictionMode.SLL)
        .or(tryParse(lexer, PredictionMode.LL));
    if (!tree.isPresent()) {
      throw new RuntimeException("Failed to parse given reviki");
    }
    return new MarkdownCreoleRenderer(_lrc).visit(_visitor.visit(tree.get())).trim();
  }

}
