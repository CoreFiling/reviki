package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RenderedPage {

  private static final Pattern RE_NEW_PAGE_CLASS = Pattern.compile("(^|\\s)new-page($|\\s)");
  private static final Pattern RE_EXIST_PAGE_CLASS = Pattern.compile("(^|\\s)existing-page($|\\s)");
  
  private final String _pageName;
  private final String _rendered; 

  public RenderedPage(final String pageName, final String rendered) {
    _pageName = pageName;
    _rendered = rendered;
  }

  public String getPage() {
    return _pageName;
  }
  
  /**
   * @return outgoing links in document order.
   * @throws IOException If we fail to parse. 
   */
  public List<String> findOutgoingWikiLinks() throws IOException {
    final List<String> outgoing = new ArrayList<String>();
    SAXParser parser = new SAXParser();
    parser.setContentHandler(new DefaultHandler() {
      public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
        if (localName.equals("A")) {
          boolean wikiPageClass = false;
          String href = null;
          for (int i = 0, len = attributes.getLength(); i < len; ++i) {
            if ("class".equals(attributes.getLocalName(i))) {
              wikiPageClass = hasWikiPageClass(attributes.getValue(i));
            }
            else if ("href".equals(attributes.getLocalName(i))) {
              href = attributes.getValue(i);
            }
          }
          if (wikiPageClass && href != null) {
            int lastSlash = href.lastIndexOf('/');
            outgoing.add(href.substring(lastSlash + 1));
          }
        }
      }
    });
    try {
      parser.parse(new InputSource(new StringReader(_rendered)));
    }
    catch (final SAXException ex) {
      throw new IOException("Parse error") {
        private static final long serialVersionUID = 1L;
        @Override
        public Throwable getCause() {
          return ex;
        }
      };
    }
    return outgoing;
  }

  private boolean hasWikiPageClass(final String clazz) {
    return RE_EXIST_PAGE_CLASS.matcher(clazz).find() || RE_NEW_PAGE_CLASS.matcher(clazz).find();
  }

}
