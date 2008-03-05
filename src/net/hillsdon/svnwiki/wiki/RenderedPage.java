package net.hillsdon.svnwiki.wiki;

import static net.hillsdon.fij.core.Functional.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class RenderedPage {

  private static final Pattern RE_NEW_PAGE_CLASS = Pattern.compile("(^|\\s)new-page($|\\s)");
  private static final Pattern RE_EXIST_PAGE_CLASS = Pattern.compile("(^|\\s)existing-page($|\\s)");
  
  private static final Collection<String> BLOCK_HTML_TAGS = set("P", "DIV", "PRE", "BLOCKQUOTE", "H1", "H2", "H3", "H4", "H5", "H6", "CENTER", "FORM", "HR", "UL", "OL", "LI");
  private static boolean isBlock(final String localName) {
    return BLOCK_HTML_TAGS.contains(localName);
  }
  
  private final String _pageName;
  private final Document _document; 

  public RenderedPage(final String pageName, final Document document) {
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

  /**
   * @return outgoing links in document order.
   */
  public List<String> findOutgoingWikiLinks() {
    final List<String> outgoing = new ArrayList<String>();
    final NodeList links = _document.getElementsByTagName("A");
    for (int i = 0, len = links.getLength(); i < len; ++i) {
      Element link = (Element) links.item(i);
      if (hasWikiPageClass(link.getAttribute("class"))) {
        String href = link.getAttribute("href");
        int lastSlash = href.lastIndexOf('/');
        outgoing.add(href.substring(lastSlash + 1));
      }
    }
    return outgoing;
  }

  private boolean hasWikiPageClass(final String clazz) {
    return RE_EXIST_PAGE_CLASS.matcher(clazz).find() || RE_NEW_PAGE_CLASS.matcher(clazz).find();
  }

}
