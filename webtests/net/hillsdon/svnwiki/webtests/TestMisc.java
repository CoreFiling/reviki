package net.hillsdon.svnwiki.webtests;

import net.hillsdon.svnwiki.web.handlers.PageHandler;

import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class TestMisc extends WebTestSupport {

  public void testAppRootRedirectsToWikiList() throws Exception {
    assertTrue(getWebPage("").getTitleText().contains("Wiki List"));
  }

  public void testWikiRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("pages/test/").getTitleText().contains("Front Page"));
    assertTrue(getWebPage("pages/test").getTitleText().contains("Front Page"));
  }
  
  public void testNoBackLinkToSelf() throws Exception {
    assertTrue(getWebPage("pages/test/FrontPage")
      .getByXPath("//a[@class='backlink' and @href = 'FrontPage']").isEmpty());
  }
  
  public void testCantPathWalk() throws Exception {
    HtmlPage page = getWebPage("pages/test/%2ERootFile");
    page.asText().contains(PageHandler.PATH_WALK_ERROR_MESSAGE);
  }
  
}
