package net.hillsdon.svnwiki.vc;

import static net.hillsdon.fij.core.Functional.set;

import java.io.StringReader;
import java.util.Collection;

import junit.framework.TestCase;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

public class TestParsedRendering extends TestCase {

  public void test() throws Exception {
    DOMParser parser = new DOMParser();
    parser.parse(new InputSource(new StringReader("<p>This is amazing!<p><p>Hello</p>")));
    Document document = parser.getDocument();
    System.err.println(htmlDomToString(document));
  }

  private static final Collection<String> BLOCK_HTML_TAGS = set("P", "DIV", "PRE", "BLOCKQUOTE", "H1", "H2", "H3", "H4", "H5", "H6", "CENTER", "FORM", "HR"); 
  
  private String htmlDomToString(final Document document) {
    StringBuilder writer = new StringBuilder();
    DocumentTraversal traversal = (DocumentTraversal) document;
    NodeIterator iterator = traversal.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null, true);
    for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        writer.append(isBlock(n.getNodeName()) ? "\n" : " ");
      }
      else {
        CharacterData data = (CharacterData) n;
        writer.append(data.getData());
      }
    }
    return writer.toString().trim();
  }

  private boolean isBlock(final String localName) {
    return BLOCK_HTML_TAGS.contains(localName);
  }
  
}
