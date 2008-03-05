package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;

import org.cyberneko.html.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RenderedPageFactory {

  private final MarkupRenderer _renderer;

  public RenderedPageFactory(final MarkupRenderer renderer) {
    _renderer = renderer;
  }
  
  public RenderedPage create(final String pageName, final String content) throws IOException, PageStoreException {
    StringWriter rendered = new StringWriter();
    _renderer.render(new PageReference(pageName), content, rendered);
    DOMParser parser = new DOMParser();
    try {
      parser.parse(new InputSource(new StringReader(rendered.toString())));
    }
    catch (final SAXException e) {
      throw new IOException("XML parse failed.") {
        private static final long serialVersionUID = 1L;
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }
    return new RenderedPage(pageName, parser.getDocument());
  }
  
}
