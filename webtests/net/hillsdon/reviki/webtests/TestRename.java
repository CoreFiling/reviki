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

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_HEADER;
import static net.hillsdon.reviki.webtests.TestAttachments.getTextAttachmentAtEndOfLink;

import java.util.List;

import net.hillsdon.reviki.vc.PageReference;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;


/**
 * Tests rename.
 *
 * @author mth
 */
public class TestRename extends AddWikiWebTestSupport {

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

  public void testRenamePageContainsHeader() throws Exception {
    // https://jira.int.corefiling.com/browse/REVIKI-642
    // Check that the header page was added for the rename page
    final PageReference headerPage = PAGE_HEADER;
    final String expect = "T" + System.currentTimeMillis() + headerPage.getPath().toLowerCase();
    editWikiPage(headerPage.getPath(), expect, "", "Some new content", null);
    try {
      String name = uniqueWikiPageName("RenamePageHeaderTest");
      HtmlPage edited = editWikiPage(name, "some content", "", "", true);
      HtmlPage renamePage = (HtmlPage) edited.getAnchorByName("rename").click();
      assertTrue(renamePage.asText().contains(expect));
    }
    finally {
      editWikiPage(headerPage.getPath(), "", "", "Tidying", null);
    }
  }

  private static final String DIRECTORY2 = "doesNotExist2";

  private void removeOurDirectoriesIfExists(final SVNRepository repository) throws SVNException {
    if (SVNNodeKind.DIR.equals(repository.checkPath(DIRECTORY, -1))) {
      removeDir(repository.getCommitEditor("Removing test directory", null), DIRECTORY);
    }
    if (SVNNodeKind.DIR.equals(repository.checkPath(DIRECTORY2, -1))) {
      removeDir(repository.getCommitEditor("Removing test directory", null), DIRECTORY2);
    }
  }

  protected void createOurDirectory2(final ISVNEditor editor) {
    try {
      editor.openRoot(-1);
      editor.addDir(DIRECTORY2, null, 0);
      editor.closeDir();
      editor.closeEdit();
    }
    catch (SVNException e) {
      abortEditAndFail(editor, e);
    }
  }

  public void testMovedPageContainsNotification() throws Exception {
    // Test that when a page is moved to a different wiki we get an appropriate notification
    // https://jira.int.corefiling.com/browse/REVIKI-552
    removeOurDirectoriesIfExists(_repository);

    final String wikiName1 = "uniqueWiki" + uniqueName();
    createOurDirectory(_repository.getCommitEditor("Creating test directory", null));
    addWiki(wikiName1, DIRECTORY);

    final String wikiName2 = "uniqueWiki" + uniqueName();
    createOurDirectory2(_repository.getCommitEditor("Creating test directory", null));
    addWiki(wikiName2, DIRECTORY2);

    String fromPageName = uniqueWikiPageName("RenameTestFrom");
    editWikiPage(getWebPage("pages/" + wikiName1 + "/" + fromPageName), "Catchy tunes", "", "Whatever", null);

    // Now move the wiki page
    String toPageName = uniqueWikiPageName("RenameTestTo");
    ISVNEditor editor = _repository.getCommitEditor("Move wiki page", null);
    try {
      editor.openRoot(-1);
      editor.openDir(DIRECTORY2, -1);
      editor.addFile(toPageName, DIRECTORY + "/" + fromPageName, -1);
      editor.closeDir();
      editor.deleteEntry(DIRECTORY + "/" + fromPageName, -1);
      editor.closeDir();
      editor.closeEdit();
    }
    catch (SVNException e) {
      abortEditAndFail(editor, e);
    }

    HtmlPage fromPage = getWebPage("pages/" + wikiName1 + "/" + fromPageName);
    HtmlPage toPage = getWebPage("pages/" + wikiName2 + "/" + toPageName);
    assertTrue(toPage.asText(), toPage.asText().contains("Catchy tunes")); // Check we really did move the page

    assertTrue(fromPage.asText(), fromPage.asText().contains("Page has been moved outside of this wiki"));
    assertTrue(fromPage.asText(), fromPage.asText().contains(toPageName));
  }
}
