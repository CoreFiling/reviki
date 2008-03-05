package net.hillsdon.svnwiki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class TestListWikis extends WebTestSupport {

  public void testWikiListContainsTestWiki() throws Exception {
    HtmlPage list = getWebPage("list");
    boolean testWikiFound = false;
    for (Object o : list.getByXPath("id('wikiList')/li")) {
      HtmlListItem li = (HtmlListItem) o;
      String href = ((HtmlAnchor) li.getByXPath("a").get(0)).getHrefAttribute();
      testWikiFound |= href.contains("pages/test/FrontPage");
    }
    assertTrue(testWikiFound);
  }
  
}
