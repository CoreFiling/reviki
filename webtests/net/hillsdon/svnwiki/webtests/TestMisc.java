package net.hillsdon.svnwiki.webtests;



public class TestMisc extends WebTestSupport {

  public void testRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("").getTitleText().contains("Front Page"));
  }
  
  public void testNoBackLinkToSelf() throws Exception {
    assertTrue(getWebPage("pages/FrontPage")
      .getByXPath("//a[@class='backlink' and @href = 'FrontPage']").isEmpty());
  }
  
}
