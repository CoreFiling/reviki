/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.webtests;

import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.ERROR_NO_FILE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class TestAttachments extends WebTestSupport {

  public static final String ATTACHMENT_UPLOAD_FILE_1 = "webtests/file1.txt";
  public static final String ATTACHMENT_UPLOAD_FILE_2 = "webtests/file2.txt";

  public static String getAttachmentAtEndOfLink(final HtmlAnchor link) throws IOException {
    UnexpectedPage attachment = (UnexpectedPage) link.click();
    BufferedReader in = new BufferedReader(new InputStreamReader(attachment.getInputStream()));
    try {
      return IOUtils.toString(in).trim();
    }
    finally {
      in.close();
    }
  }

  public void testGetAttachmentThatDoesntExistGives404() throws Exception {
    try {
      getWebPage("pages/test/FrontPage/attachments/DoesntExist.txt");
      fail();
    }
    catch (FailingHttpStatusCodeException ex) {
      assertEquals(404, ex.getStatusCode());
    }
  }

  public void testUploadNothingGivesError() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "", "", true);
    HtmlPage attachments = clickAttachmentsLink(page, name);
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    attachments = (HtmlPage) form.getInputByValue("Upload").click();
    assertEquals(ERROR_NO_FILE, ((DomNode) attachments.getByXPath("id('flash')").get(0)).asText().trim());
  }
  
  public void testUploadAndDownloadAttachment() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "Content", "", true);

    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);

    assertEquals("File 1.", getAttachmentAtEndOfLink(attachments.getAnchorByHref("file.txt")));

    // A link should have been added to the page.
    page = getWikiPage(name);
    assertEquals("File 1.", getAttachmentAtEndOfLink(page.getAnchorByHref(name + "/attachments/file.txt")));
    
    attachments = clickAttachmentsLink(page, name);
    HtmlForm form = attachments.getFormByName("replaceAttachmentUpload");
    form.getInputByName("file").setValueAttribute(ATTACHMENT_UPLOAD_FILE_2);
    attachments = (HtmlPage) form.getInputByValue("Upload new version").click();
    assertEquals("File 2.", getAttachmentAtEndOfLink(page.getAnchorByHref(name + "/attachments/file.txt")));

    HtmlAnchor previousRevision = (HtmlAnchor) attachments.getByXPath("//a[starts-with(@href, 'file.txt?revision')]").get(0);
    assertEquals("File 1.", getAttachmentAtEndOfLink(previousRevision));
  }

}
