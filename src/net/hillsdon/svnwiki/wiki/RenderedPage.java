package net.hillsdon.svnwiki.wiki;

import static net.hillsdon.fij.core.Functional.set;

import java.util.Collection;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class RenderedPage {

  private static final Collection<String> BLOCK_HTML_TAGS = set("P", "DIV", "PRE", "BLOCKQUOTE", "H1", "H2", "H3", "H4", "H5", "H6", "CENTER", "FORM", "HR", "UL", "OL", "LI");
  private static boolean isBlock(final String localName) {
    return BLOCK_HTML_TAGS.contains(localName);
  }
  
  private final String _pageName;
  private final Document _document; 

  public RenderedPage(String pageName, Document document) {
    _pageName = pageName;
    _document = document;
  }

  public String getPage() {
    return _pageName;
  }
  
  public String asText() {
    StringBuilder writer = new StringBuilder();
    DocumentTraversal traversal = (DocumentTraversal) _document;
    NodeIterator iterator = traversal.createNodeIterator(_document.getDocumentElement(), NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT, null, true);
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

}
