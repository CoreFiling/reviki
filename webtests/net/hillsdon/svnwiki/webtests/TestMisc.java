package net.hillsdon.svnwiki.webtests;


public class TestMisc extends WebTestSupport {

  public void testRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("").getTitleText().contains("Front Page"));
  }
  
}
