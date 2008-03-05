package net.hillsdon.svnwiki.webtests;



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
  
}
