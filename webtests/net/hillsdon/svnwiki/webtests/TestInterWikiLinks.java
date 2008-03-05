package net.hillsdon.svnwiki.webtests;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class TestInterWikiLinks extends WebTestSupport {

  /**
   * Add an interwiki link mapping, create a link, make sure it points where we expect.
   */
  public void test() throws Exception {
    editWikiPage("ConfigInterWikiLinks", "foo http://www.example.com/Wiki?%s", "Link mapping.", null);
    HtmlPage page = editWikiPage(uniqueWikiPageName("InterWikiLinkTest"), "foo:1234", "Add inter-wiki link.", true);
    HtmlAnchor link = page.getAnchorByHref("http://www.example.com/Wiki?1234");
    assertEquals("foo:1234", link.asText());
  }
  
}
