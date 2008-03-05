package net.hillsdon.svnwiki.webtests;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestEditing extends WebTestSupport {
  
  private static final Pattern RE_REVISION = Pattern.compile("r[0-9]+");
  
  public void testEditPageIncrementsRevision() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    HtmlPage initial = editWikiPage(name, "Initial content", "", true);
    long initialRevision = getRevisionNumberFromTitle(initial);
    HtmlPage edited = editWikiPage(name, "Initial content.  Extra content.", "", false);
    assertEquals(initialRevision + 1, getRevisionNumberFromTitle(edited));
  }
  
  private long getRevisionNumberFromTitle(final HtmlPage page) {
    Matcher matcher = RE_REVISION.matcher(page.getTitleText());
    assertTrue(matcher.find());
    long revision = Long.parseLong(matcher.group().substring(1));
    return revision;
  }
  
  public void testCancelEditNewPage() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    editThenCancel(name);
  }
  
  public void testCancelEditExistingPage() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    editWikiPage(name, "Whatever", "", true);
    editThenCancel(name);
  }

  private void editThenCancel(final String name) throws IOException {
    final String flagText = "Should not be saved.";
    HtmlPage editPage = (HtmlPage) getWebPage("pages/" + name).getFormByName("editForm").getInputByValue("Edit").click();
    HtmlForm form = editPage.getFormByName("editForm");
    form.getTextAreaByName("content").setText(flagText);
    HtmlPage viewPage = (HtmlPage) form.getInputByValue("Cancel").click();
    assertFalse(viewPage.asText().contains(flagText));
  }
  
}
