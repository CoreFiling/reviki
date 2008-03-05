package net.hillsdon.svnwiki.webtests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


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
  
  public void testUploadAndDownloadAttachment() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "", "", true);
    HtmlAnchor attachmentsLink = page.getAnchorByHref(name + "/attachments/");
    assertEquals("Attachments", attachmentsLink.asText());
    HtmlPage attachments = (HtmlPage) attachmentsLink.click();
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    form.getInputByName("file").setValueAttribute("build.xml");
    attachments = (HtmlPage) form.getInputByValue("Upload").click();
    checkAttachmentIsAtEndOfLink(attachments.getAnchorByHref("build.xml"));
    
    page = editWikiPage(name, "{attached:build.xml}", "Linked to attachment", false);
    checkAttachmentIsAtEndOfLink(page.getAnchorByHref(name + "/attachments/build.xml"));
  }

  private void checkAttachmentIsAtEndOfLink(HtmlAnchor link) throws IOException {
    UnexpectedPage attachment = (UnexpectedPage) link.click();
    BufferedReader in = new BufferedReader(new InputStreamReader(attachment.getInputStream()));
    try {
      assertTrue(in.readLine().startsWith("<project name="));
    }
    finally {
      in.close();
    }
  }
  
}
