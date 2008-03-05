package net.hillsdon.svnwiki.wiki;

import static net.hillsdon.fij.core.Functional.set;

import java.util.Collection;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class RenderedPage {

  private static final Collection<String> BLOCK_HTML_TAGS = set("P", "DIV", "PRE", "BLOCKQUOTE", "H1", "H2", "H3", "H4", "H5", "H6", "CENTER", "FORM", "HR");
  private static boolean isBlock(final String localName) {
    return BLOCK_HTML_TAGS.contains(localName);
  }
  
  private final PageInfo _page;
  private final Document _document; 

  public RenderedPage(PageInfo page, Document document) {
    _page = page;
    _document = document;
  }

  public PageReference getPage() {
    return _page;
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
