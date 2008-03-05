package net.hillsdon.svnwiki.webtests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestEditing extends WebTestSupport {
  
  private static final Pattern RE_REVISION = Pattern.compile("r[0-9]+");
  
  public void testEditPageIncrementsRevision() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    HtmlPage initial = editWikiPage(name, "Initial content", true);
    long initialRevision = getRevisionNumberFromTitle(initial);
    HtmlPage edited = editWikiPage(name, "Initial content.  Extra content.", false);
    assertEquals(initialRevision + 1, getRevisionNumberFromTitle(edited));
  }

  private long getRevisionNumberFromTitle(final HtmlPage page) {
    Matcher matcher = RE_REVISION.matcher(page.getTitleText());
    assertTrue(matcher.find());
    long revision = Long.parseLong(matcher.group().substring(1));
    return revision;
  }
  
}
