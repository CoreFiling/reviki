package net.hillsdon.svnwiki.webtests;

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;


public class TestHistory extends WebTestSupport {

  @SuppressWarnings("unchecked")
  public void test() throws Exception {
    String pageName = uniqueWikiPageName("HistoryTest");
    HtmlPage page = editWikiPage(pageName, "Initial content", "", true);
    page = editWikiPage(pageName, "Altered content", "s/Initial/Altered", false);
    HtmlPage history = (HtmlPage) page.getAnchorByHref("?history").click();
    List<HtmlTableRow> historyRows = history.getByXPath("//tr[td]");
    assertEquals(2, historyRows.size());
    HtmlTableRow altered = historyRows.get(0);
    // First column is date/time.
    assertEquals(getUsername(), altered.getCell(1).asText());
    assertEquals("s/Initial/Altered", altered.getCell(2).asText());
    HtmlTableRow initial = historyRows.get(1);
    assertEquals(getUsername(), initial.getCell(1).asText());
    assertEquals("None", initial.getCell(2).asText());

    HtmlAnchor diffLink = (HtmlAnchor) altered.getCell(2).getByXPath("a").iterator().next();
    HtmlPage diff = (HtmlPage) diffLink.click();
    assertEquals("Altered", ((DomNode) diff.getByXPath("//ins").iterator().next()).asText());
    assertEquals("Initial", ((DomNode) diff.getByXPath("//del").iterator().next()).asText());
  }
  
}
