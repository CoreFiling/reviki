package net.hillsdon.svnwiki.webtests;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Iterator;

import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestRecentChanges extends WebTestSupport {
  
  /**
   * Create two new pages.
   * Ensure they appear at the top of RecentChanges with the second page first.
   * Edit the first page.
   * Ensure it it now at the top of recent changes.
   */
  public void testRecentChanges() throws Exception {
    String createdFirst = uniqueWikiPageName("RecentChangesTestFirst");
    editWikiPage(createdFirst, "", "", true);
    String createdSecond = uniqueWikiPageName("RecentChangesTestSecond");
    editWikiPage(createdSecond, "", "", true);
    
    Iterator<HtmlAnchor> links = getRecentChangesLinks();
    HtmlAnchor first = links.next();
    HtmlAnchor second = links.next();
    assertEquals(first.asText(), createdSecond);
    assertEquals(second.asText(), createdFirst);
    
    String descriptionOfChange = format("Bump %s up to top.", createdFirst);
    editWikiPage(createdFirst, "", descriptionOfChange, false);
    links = getRecentChangesLinks();
    first = links.next();
    second = links.next();
    assertEquals(first.asText(), createdFirst);
    assertEquals(second.asText(), createdSecond);
    
    HtmlPage recentChanges = getWebPage("pages/RecentChanges");
    recentChanges.asText().contains(descriptionOfChange);
  }

  @SuppressWarnings("unchecked")
  private Iterator<HtmlAnchor> getRecentChangesLinks() throws IOException, JaxenException {
    HtmlPage recentChanges = getWebPage("pages/RecentChanges");
    Iterator<HtmlAnchor> links = recentChanges.getByXPath("//tr/td[position() = 2]/a").iterator();
    return links;
  }
  
}
