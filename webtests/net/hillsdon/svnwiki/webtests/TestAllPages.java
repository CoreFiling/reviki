package net.hillsdon.svnwiki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class TestAllPages extends WebTestSupport {
  
  /**
   * Create a page.
   * Ensure it appears in AllPages.
   */
  public void testAllPages() throws Exception {
    String name = uniqueWikiPageName("AllPagesTest");
    editWikiPage(name, "Should appear in all pages", "", true);

    HtmlPage allPages = getWebPage("pages/test/AllPages");
    assertTrue(allPages.getTitleText().endsWith("All Pages"));
    HtmlAnchor link = allPages.getAnchorByHref(name);
    assertEquals(name, link.asText());
  }
  
}
