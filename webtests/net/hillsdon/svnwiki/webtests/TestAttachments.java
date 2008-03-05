package net.hillsdon.svnwiki.webtests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

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
    final String file1 = "webtests/file1.txt";
    final String file2 = "webtests/file2.txt";
    
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "", "", true);
    HtmlPage attachments = clickAttachmentsLink(page, name);
    System.err.println(attachments.asXml());
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    form.getInputByName("file").setValueAttribute(file1);
    form.getInputByName("attachmentName").setValueAttribute("file");
    attachments = (HtmlPage) form.getInputByValue("Upload").click();
    assertEquals("File 1.", getAttachmentAtEndOfLink(attachments.getAnchorByHref("file.txt")));
    
    page = editWikiPage(name, "{attached:file.txt}", "Linked to attachment", false);
    assertEquals("File 1.", getAttachmentAtEndOfLink(page.getAnchorByHref(name + "/attachments/file.txt")));
    
    attachments = clickAttachmentsLink(page, name);
    form = attachments.getFormByName("replaceAttachmentUpload");
    form.getInputByName("file").setValueAttribute(file2);
    attachments = (HtmlPage) form.getInputByValue("Upload new version").click();
    assertEquals("File 2.", getAttachmentAtEndOfLink(page.getAnchorByHref(name + "/attachments/file.txt")));
  }

  private HtmlPage clickAttachmentsLink(final HtmlPage page, final String name) throws IOException {
    HtmlAnchor attachmentsLink = page.getAnchorByHref(name + "/attachments/");
    assertEquals("Attachments", attachmentsLink.asText());
    HtmlPage attachments = (HtmlPage) attachmentsLink.click();
    return attachments;
  }

  private String getAttachmentAtEndOfLink(final HtmlAnchor link) throws IOException {
    UnexpectedPage attachment = (UnexpectedPage) link.click();
    BufferedReader in = new BufferedReader(new InputStreamReader(attachment.getInputStream()));
    try {
      return IOUtils.toString(in).trim();
    }
    finally {
      in.close();
    }
  }
  
}
