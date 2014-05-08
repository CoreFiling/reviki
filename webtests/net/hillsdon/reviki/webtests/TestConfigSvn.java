package net.hillsdon.reviki.webtests;

import junit.framework.AssertionFailedError;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * Tests for setting up new wikis.
 *
 * @author pjt
 */
public class TestConfigSvn extends AddWikiWebTestSupport {

  private void removeOurDirectoryIfExists(final SVNRepository repository) throws SVNException {
    if (SVNNodeKind.DIR.equals(repository.checkPath(DIRECTORY, -1))) {
      removeDir(repository.getCommitEditor("Removing test directory", null), DIRECTORY);
    }
  }

  public void testConfigSvnWithExistingDir() throws Exception {
    final String wikiName = "uniqueWiki" + uniqueName();
    removeOurDirectoryIfExists(_repository);
    createOurDirectory(_repository.getCommitEditor("Creating test directory", null));
    HtmlPage frontPage = addWiki(wikiName, DIRECTORY);

    // Perform an edit to make sure
    final String text = "Some text";
    frontPage = editWikiPage(frontPage, text, "Some change", "", null);
    assertTrue(frontPage.asText().contains(text));
  }

  public void testConfigSvnWithoutExistingDir() throws Exception {
    final String wikiName = "uniqueWiki" + uniqueName();
    removeOurDirectoryIfExists(_repository);
    final HtmlPage confSvnPage = getWebPage("pages/" + wikiName);
    // Enter svn url + DIRECTORY
    final HtmlInput input = (HtmlInput) confSvnPage.getByXPath("//input[@name='url']").iterator().next();
    input.setValueAttribute(getSvnLocation() + DIRECTORY);
    // Click Save and hopefully get same page but with flash
    final HtmlPage confPageWithFlash = (HtmlPage) ((HtmlSubmitInput) confSvnPage.getByXPath("//input[@type='submit' and @value='Save']").iterator().next()).click();
    assertTrue(confPageWithFlash.getTitleText().contains("Config Svn"));
    assertTrue(hasErrorMessage(confPageWithFlash));
  }

}
