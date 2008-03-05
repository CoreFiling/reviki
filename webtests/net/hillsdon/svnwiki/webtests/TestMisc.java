package net.hillsdon.svnwiki.webtests;



public class TestMisc extends WebTestSupport {

  public void testRootRedirectsToFrontPage() throws Exception {
    // Now we have subwikis it isn't clear what we ought to do...
    //assertTrue(getWebPage("").getTitleText().contains("Front Page"));
  }
  
  public void testNoBackLinkToSelf() throws Exception {
    assertTrue(getWebPage("pages/test/FrontPage")
      .getByXPath("//a[@class='backlink' and @href = 'FrontPage']").isEmpty());
  }
  
}
