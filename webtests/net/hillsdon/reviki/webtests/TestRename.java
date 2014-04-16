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

import static net.hillsdon.reviki.webtests.TestAttachments.getTextAttachmentAtEndOfLink;

import java.util.List;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;


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
    HtmlPage page = editWikiPage(fromPageName, "Catchy tunes", "", "Whatever", true);
    uploadAttachment(TestAttachments.ATTACHMENT_UPLOAD_FILE_1, fromPageName);

    page = renamePage(fromPageName, toPageName);

    assertTrue(page.getWebResponse().getWebRequest().getUrl().toURI().getPath().contains(toPageName));
    assertTrue(page.asText().contains("Catchy tunes"));
    page = clickAttachmentsLink(page, toPageName);
    assertEquals("File 1.", getTextAttachmentAtEndOfLink(getAnchorByHrefContains(page, "file.txt")));

    assertSearchDoesNotFindPage(page, fromPageName);
    editWikiPage(fromPageName, "This checks old page is new.", "", "Whatever", true);
  }
  
  @SuppressWarnings("unchecked")
  public void testRenameCommitMessage() throws Exception {
    String fromPageName = uniqueWikiPageName("RenameTestFrom");
    String toPageName = uniqueWikiPageName("RenameTestTo");
    HtmlPage page = editWikiPage(fromPageName, "Catchy tunes", "", "Whatever", true);
    page = renamePage(fromPageName, toPageName);
    
    HtmlPage history = (HtmlPage) ((HtmlAnchor) page.getByXPath("//a[@name='history']").iterator().next()).click();
    List<HtmlTableRow> historyRows = (List<HtmlTableRow>) history.getByXPath("//tr[td]");
    assertEquals(3, historyRows.size());
    HtmlTableRow renameInfo = historyRows.get(1);
    assertTrue(renameInfo.getCell(3).asText().contains(fromPageName));
    assertTrue(renameInfo.getCell(3).asText().contains(toPageName));
  }

  public void testRenamedPageContainsNotification() throws Exception {
    String fromPageName = uniqueWikiPageName("RenameTestFrom");
    String toPageName = uniqueWikiPageName("RenameTestTo");
    editWikiPage(fromPageName, "Catchy tunes", "", "Whatever", true);
    
    renamePage(fromPageName, toPageName);
    HtmlPage fromPage = getWikiPage(fromPageName);
    
    assertTrue(fromPage.asText().contains(toPageName));
    
    // If fromPage is subsequently edited then we no longer show the notification
    fromPage = editWikiPage(fromPageName, "another edit", "", "Whatever", false);
    assertFalse(fromPage.asText().contains(toPageName));
  }
}
