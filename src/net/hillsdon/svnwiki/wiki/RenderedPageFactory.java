package net.hillsdon.svnwiki.wiki;

import java.io.StringReader;
import java.io.StringWriter;

import net.hillsdon.svnwiki.vc.PageInfo;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class RenderedPageFactory {

  private final MarkupRenderer _renderer;

  public RenderedPageFactory(final MarkupRenderer renderer) {
    _renderer = renderer;
  }
  
  public RenderedPage create(final PageInfo page) throws Exception {
    StringWriter rendered = new StringWriter();
    _renderer.render(page, page.getContent(), rendered);
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(new StringReader(rendered.toString())));
    Document document = parser.getDocument();
    return new RenderedPage(page, document);
  }
  
}
