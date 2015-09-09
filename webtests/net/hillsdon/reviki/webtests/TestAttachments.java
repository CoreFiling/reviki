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
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_HEADER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.hillsdon.reviki.vc.AttachmentHistory;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.StoreKind;

import org.apache.commons.io.IOUtils;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDeletedText;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestAttachments extends WebTestSupport {

  public static final String ATTACHMENT_UPLOAD_FILE_1 = "webtests/file1.txt";
  public static final String ATTACHMENT_UPLOAD_FILE_2 = "webtests/file2.txt";
  public static final String ATTACHMENT_UPLOAD_FILE_3 = "webtests/a file with spaces.txt";
  public static final String ATTACHMENT_UPLOAD_FILE_4 = "webtests/afilewith\"doublequote.txt";

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

  public static String getTextAttachmentAtEndOfLink(final HtmlAnchor link) throws IOException {
    TextPage attachment = (TextPage) link.click();
    return attachment.getContent().trim();
  }
  
  public void testGetAttachmentWithNoResults() {
    final Map<String, AttachmentHistory> results = new LinkedHashMap<String, AttachmentHistory>();
    ChangeInfo ci = new ChangeInfo("k", "k", "k", new java.util.Date(), 10000, "ro", StoreKind.ATTACHMENT, null, null, 1221221);
    AttachmentHistory history = results.get(ci.getName());
  }

  public void testGetAttachmentThatDoesntExistGives404() throws Exception {
    ignoreStatusCodeErrors();
    final HtmlPage page = getWebPage("pages/test/FrontPage/attachments/DoesntExist.txt");
    assertTrue(page.getTitleText().contains("Error"));
    assertEquals(404, page.getWebResponse().getStatusCode());
  }

  public void testUploadNothingGivesError() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "content", "", "", true);
    HtmlPage attachments = clickAttachmentsLink(page, name);
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    attachments = (HtmlPage) form.getButtonByName("upload").click();
    assertEquals(ERROR_NO_FILE, getErrorMessage(attachments));
  }

  public void testUploadAndDownloadAttachment() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "Content", "", "", true);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "file.txt")));

    // A link should have been added to the page.
    page = getWikiPage(name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "/attachments/file.txt")));

    attachments = clickAttachmentsLink(page, name);
    HtmlForm form = attachments.getFormByName("replaceAttachmentUpload");
    form.getInputByName("file").setValueAttribute(ATTACHMENT_UPLOAD_FILE_2);
    attachments = (HtmlPage) form.getInputByValue("Upload").click();
    assertEquals("File 2.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "/attachments/file.txt")));

    HtmlAnchor previousRevision = (HtmlAnchor) attachments.getByXPath("//a[contains(@href, '?revision')]").get(0);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(previousRevision));
  }

  public void testUploadAndDownloadAttachmentWithWhitespace() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTestEscapes");
    HtmlPage page = editWikiPage(name, "Content", "", "", true);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_3, name, "a file with spaces.txt", "");
    assertEquals("File 3.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "a%20file%20with%20spaces.txt")));

    // A link should have been added to the page.
    page = getWikiPage(name);
    assertEquals("File 3.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "/attachments/a%20file%20with%20spaces")));
  }

  //public void testUploadAndDownloadAttachmentWithDoubleQuotes() throws Exception {
  //  String name = uniqueWikiPageName("AttachmentsTestQuotes");
  //  HtmlPage page = editWikiPage(name, "Content", "", "", true);
  //  HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_4, name, "afilewith\"doublequote.txt", "");
  //  assertEquals("File 4.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "afilewith%22doublequote.txt")));
  //
  //  // A link should have been added to the page.
  //  page = getWikiPage(name);
  //  assertEquals("File 4.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "/attachments/afilewith%22doublequote")));
  //}

  public void testUploadAndDownloadAttachmentOnNewPage() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = getWikiPage(name);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "file.txt")));

    // A link should have been added to the page.
    page = getWikiPage(name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "/attachments/file.txt")));
  }

  public void testUploadAndDeleteAttachment() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "Content", "", "", true);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "file.txt")));

    page = getWikiPage(name);
    attachments = clickAttachmentsLink(page, name);
    HtmlForm deleteForm = attachments.getFormByName("deleteAttachment");
    attachments = deleteForm.getButtonByName("delete").click();

    // There shouldn't be any link directly to the attachment
    for(Object o: attachments.getByXPath("//a[contains(@href, 'file.txt')]")) {
      HtmlAnchor anchor = (HtmlAnchor) o;
      assertEquals(true, anchor.getHrefAttribute().contains("?revision"));
    }

    // Previous version should still be available
    HtmlAnchor previousRevision = (HtmlAnchor) attachments.getByXPath("//a[contains(@href, '?revision')]").get(0);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(previousRevision));
  }
  
  @SuppressWarnings("unchecked")
  public void testUploadRenameAndDeleteAttachment() throws Exception {
    // https://bugs.corefiling.com/show_bug.cgi?id=13574
    String name = uniqueWikiPageName("AttachmentsTest");
    HtmlPage page = editWikiPage(name, "Content", "", "", true);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(attachments, "file.txt")));

    // Rename page in SVN
    String newName = name + "Renamed";
    renamePage(name, newName);

    // Now try deleting the attachment from the new page, note that this means that the only entry in the attachment's history will be the deletion as other log entries are at the old path
    page = getWikiPage(newName);
    attachments = clickAttachmentsLink(page, newName);

    HtmlForm deleteForm = attachments.getFormByName("deleteAttachment");
    attachments = deleteForm.getButtonByName("delete").click();

    assertFalse("Page should not be completely empty!", attachments.asText().isEmpty());

    // There shouldn't be any link directly to the attachment
    for(Object o: attachments.getByXPath("//a[contains(@href, 'file.txt')]")) {
      HtmlAnchor anchor = (HtmlAnchor) o;
      assertEquals(true, anchor.getHrefAttribute().contains("?revision"));
    }

    // Previous version should still be available
    List<HtmlDeletedText> previousRevisions = (List<HtmlDeletedText>) attachments.getByXPath("//span[substring(text(), 1, 8)='file.txt']");
    assertEquals(1, previousRevisions.size());
  }
  
  public void testAttachmentsPageContainsHeader() throws Exception {
    // https://jira.int.corefiling.com/browse/REVIKI-642
    // Check that the header page was added for the attachements page
    final PageReference headerPage = PAGE_HEADER;
    final String expect = "T" + System.currentTimeMillis() + headerPage.getPath().toLowerCase();
    editWikiPage(headerPage.getPath(), expect, "", "Some new content", null);
    try {
      String name = uniqueWikiPageName("AttachmentPageHeaderTest");
      HtmlPage edited = editWikiPage(name, "some content", "", "", true);
      HtmlPage attachmentsPage = clickAttachmentsLink(edited, name);
      assertTrue(attachmentsPage.asText().contains(expect));
    }
    finally {
      editWikiPage(headerPage.getPath(), "", "", "Tidying", null);
    }
  }

  public void testUploadAttachmentWithDefaultMessage() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    editWikiPage(name, "Content", "", "", true);
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name);
    assertTrue(attachments.asText().contains("Added attachment file.txt"));
  }

  public void testUploadAttachmentWithCustomMessage() throws Exception {
    String name = uniqueWikiPageName("AttachmentsTest");
    editWikiPage(name, "Content", "", "", true);
    String message = "Some custom message";
    HtmlPage attachments = uploadAttachment(ATTACHMENT_UPLOAD_FILE_1, name, message);
    assertTrue(attachments.asText().contains(message));
  }
}
