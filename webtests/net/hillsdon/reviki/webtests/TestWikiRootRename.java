package net.hillsdon.reviki.webtests;

import java.util.List;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

/**
 * https://bugs.corefiling.com/show_bug.cgi?id=5912 
 * This test shares a lot of code with TestConfigSvn.
 */
public class TestWikiRootRename extends AddWikiWebTestSupport {
  private static final String DIRECTORY2 = "doesNotExist2";

  private void removeOurDirectoriesIfExists(final SVNRepository repository) throws SVNException {
    if (SVNNodeKind.DIR.equals(repository.checkPath(DIRECTORY, -1))) {
      removeDir(repository.getCommitEditor("Removing test directory", null), DIRECTORY);
    }
    if (SVNNodeKind.DIR.equals(repository.checkPath(DIRECTORY2, -1))) {
      removeDir(repository.getCommitEditor("Removing test directory", null), DIRECTORY2);
    }
  }

  public void testWikiRootRename() throws Exception {
    final String wikiName = "uniqueWiki" + uniqueName();
    removeOurDirectoriesIfExists(_repository);
    createOurDirectory(_repository.getCommitEditor("Creating test directory", null));
    HtmlPage frontPage = addWiki(wikiName, DIRECTORY);

    // Perform an edit to make sure
    final String text = "Some text";
    frontPage = editWikiPage(frontPage, text, "", "Some change", null);
    assertTrue(frontPage.asText().contains(text));

    final String text2 = "Some text again";
    frontPage = editWikiPage(frontPage, text2, "", "Some change 2", null);
    assertTrue(frontPage.asText().contains(text2));

    final String otherPageText = "This is another page";
    HtmlPage otherPage = getWebPage("pages/" + wikiName + "/OtherPage");
    frontPage = editWikiPage(otherPage, otherPageText, "", "Create other page", null);

    // Now rename the Wiki's SVN location
    ISVNEditor editor = _repository.getCommitEditor("Rename wiki root", null);
    try {
      editor.openRoot(-1);
      editor.addDir(DIRECTORY2, DIRECTORY, -1);
      editor.closeDir();
      editor.deleteEntry(DIRECTORY, -1);
      editor.closeDir();
      editor.closeEdit();
    }
    catch (SVNException e) {
      abortEditAndFail(editor, e);
    }

    final String wikiName2 = "uniqueWiki" + uniqueName();
    frontPage = addWiki(wikiName2, DIRECTORY2);

    // Check we've got the same front page still
    assertTrue(frontPage.asText().contains(text2));

    // Check that the OtherPage appears in the AllPages list even though it hasn't been updated since the copy
    HtmlPage allPages = getWebPage("pages/" + wikiName2 +  "/AllPages");
    assertTrue(allPages.getTitleText().endsWith("All Pages"));
    HtmlAnchor link = getAnchorByHrefContains(allPages, BASE_URL + "/pages/" + wikiName2 + "/OtherPage");
    assertEquals("OtherPage", link.asText());

    // Now edit it again
    final String text3 = "Some more stuff";
    frontPage = editWikiPage(frontPage, text3, "", "Some change 3", null);
    assertTrue(frontPage.asText(), frontPage.asText().contains(text3));

    // Do we have the complete history?
    HtmlPage history = (HtmlPage) ((HtmlAnchor) frontPage.getByXPath("//a[@name='history']").iterator().next()).click();
    List<HtmlTableRow> historyRows = (List<HtmlTableRow>) history.getByXPath("//tr[td]");
    assertEquals(4, historyRows.size());

    // Can we diff past the wiki root rename?
    final List<HtmlRadioButtonInput> radioButtons = (List<HtmlRadioButtonInput>) history.getByXPath("//input[@type='radio']/.");
    assertEquals(6, radioButtons.size());
    radioButtons.get(1).click();
    radioButtons.get(4).click();
    final List<HtmlSubmitInput> compareButtons = (List<HtmlSubmitInput>) history.getByXPath("//input[@type='submit' and @value='Compare']/.");
    assertEquals(1, compareButtons.size());
    HtmlPage diff = (HtmlPage) compareButtons.get(0).click();
    // The prefix "Some " occurs in both revisions being compared
    assertEquals("more stuff", ((DomNode) diff.getByXPath("//ins").iterator().next()).asText());
    assertEquals("text", ((DomNode) diff.getByXPath("//del").iterator().next()).asText());
  }
}
