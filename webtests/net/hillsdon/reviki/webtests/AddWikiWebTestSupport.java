package net.hillsdon.reviki.webtests;

import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.tmatesoft.svn.core.SVNException;
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

public abstract class AddWikiWebTestSupport extends WebTestSupport {

  protected static final String DIRECTORY = "doesNotExist";
  protected SVNRepository _repository;

  public AddWikiWebTestSupport() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DAVRepositoryFactory.setup();
    _repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(getSvnLocation()));
    ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(getUsername(), getPassword());
    _repository.setAuthenticationManager(authManager);
  }

  protected void removeDir(final ISVNEditor editor, final String directory) {
    try {
      editor.openRoot(-1);
      editor.deleteEntry(directory, -1);
      editor.closeDir();
      editor.closeEdit();
    }
    catch (SVNException e) {
      abortEditAndFail(editor, e);
    }
  }

  protected void createOurDirectory(final ISVNEditor editor) {
    try {
      editor.openRoot(-1);
      editor.addDir(DIRECTORY, null, 0);
      editor.closeDir();
      editor.closeEdit();
    }
    catch (SVNException e) {
      abortEditAndFail(editor, e);
    }
  }

  protected void abortEditAndFail(final ISVNEditor editor, final SVNException originalError) {
    try {
      editor.abortEdit();
    }
    catch (SVNException e) {
    }
    AssertionFailedError fail = new AssertionFailedError();
    fail.initCause(originalError);
    throw fail;
  }

  protected HtmlPage addWiki(final String wikiName, final String directory) throws IOException {
    final HtmlPage confSvnPage = getWebPage("pages/" + wikiName);
    // Enter svn url + directory
    HtmlInput input = (HtmlInput) confSvnPage.getByXPath("//input[@name='url']").iterator().next();
    input.setValueAttribute(getSvnLocation() + directory);
    // Enter svn username and password
    input = (HtmlInput) confSvnPage.getByXPath("//input[@name='user']").iterator().next();
    input.setValueAttribute(getUsername());
    input = (HtmlInput) confSvnPage.getByXPath("//input[@name='pass']").iterator().next();
    input.setValueAttribute(getPassword());
    // Click Save and hopefully get FrontPage
    HtmlPage frontPage = (HtmlPage) ((HtmlSubmitInput) confSvnPage.getByXPath("//input[@type='submit' and @value='Save']").iterator().next()).click();
    assertTrue(frontPage.getTitleText(), frontPage.getTitleText().contains("Front Page"));
    return frontPage;
  }

}