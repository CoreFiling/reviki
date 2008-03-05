package net.hillsdon.svnwiki.webtests;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;


public class TestAttachments extends WebTestSupport {

  public void testGetAttachmentThatDoesntExistGives404() throws Exception {
    try {
      getWebPage(getUrl("FrontPage/attachments/DoesntExist.txt"));
      fail();
    }
    catch (FailingHttpStatusCodeException ex) {
      assertEquals(404, ex.getStatusCode());
    }
  }
  
}
