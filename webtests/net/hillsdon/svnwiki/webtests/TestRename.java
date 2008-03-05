/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.webtests;

import static net.hillsdon.svnwiki.webtests.TestAttachments.getAttachmentAtEndOfLink;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Tests rename.
 * 
 * @author mth
 */
public class TestRename extends WebTestSupport {
  
  public void testRenameLinkNotAvailableForNonExistantPages() throws Exception {
    HtmlPage page = getWikiPage(uniqueWikiPageName("RenameLinkTest"));
    try {
      page.getAnchorByName("rename");
      fail();
    }
    catch (ElementNotFoundException expected) {
    }
  }
  
  public void testRenameRenamesBothPageAndMovesAttachments() throws Exception {
    String fromPageName = uniqueWikiPageName("RenameTestFrom");
    String toPageName = uniqueWikiPageName("RenameTestTo");
    HtmlPage page = editWikiPage(fromPageName, "Catchy tunes", "Whatever", true);
    uploadAttachment(TestAttachments.ATTACHMENT_UPLOAD_FILE_1, fromPageName);
    
    page = (HtmlPage) page.getAnchorByName("rename").click();
    HtmlForm form = page.getFormByName("renameForm");
    form.getInputByName("toPage").setValueAttribute(toPageName);
    page = (HtmlPage) form.getInputByName("rename").click();

    assertTrue(page.getWebResponse().getUrl().toURI().getPath().endsWith(toPageName));
    assertTrue(page.asText().contains("Catchy tunes"));
    page = clickAttachmentsLink(page, toPageName);
    System.err.println(page.asXml());
    assertEquals("File 1.", getAttachmentAtEndOfLink(page.getAnchorByHref("file.txt")));
    
    editWikiPage(fromPageName, "This checks old page is new.", "Whatever", true);
  }
  
}
