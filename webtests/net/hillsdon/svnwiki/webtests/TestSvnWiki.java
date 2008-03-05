package net.hillsdon.svnwiki.webtests;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.text.WikiWordUtils;

import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestSvnWiki extends TestCase {

  private static final String BASE_URL = "http://localhost:8080/svnwiki";
  
  private WebClient _client; 
  
  private static String getUrl(final String path) {
    return BASE_URL + "/" + path;
  }

  @Override
  protected void setUp() throws Exception {
    _client = new WebClient();
    DefaultCredentialsProvider credentials = new DefaultCredentialsProvider();
    credentials.addCredentials(System.getProperty("wiki.user"), System.getProperty("wiki.password"));
    _client.setCredentialsProvider(credentials);
    _client.setRedirectEnabled(true);
  }

  private HtmlPage getWebPage(final String path) throws IOException {
    return (HtmlPage) _client.getPage(getUrl(path));
  }

  private int _counter = 0;
  public String wikiPageName(final String prefix) {
    return prefix + System.currentTimeMillis() + _counter++;
  }
  
  public HtmlPage editWikiPage(final String name, final String content, final boolean isNew) throws IOException {
    HtmlPage page = getWebPage("pages/" + name);
    URL pageUrl = page.getWebResponse().getUrl();
    assertTrue(!isNew ^ page.getTitleText().endsWith(" - New"));
    page = (HtmlPage) page.getFormByName("editForm").getInputByValue("Edit").click();
    HtmlForm editForm = page.getFormByName("editForm");
    editForm.getTextAreaByName("content").setText(content);
    page = (HtmlPage) editForm.getInputByValue("Save").click();
    assertEquals(pageUrl, page.getWebResponse().getUrl());
    // Only holds true if there's no mark-up...
    page.getWebResponse().getContentAsString().contains(content);
    return page;
  }
  
  public void testRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("").getTitleText().contains("Front Page"));
  }
  
  /**
   * Create a page.
   * Ensure it appears in AllPages.
   */
  public void testAllPages() throws Exception {
    String name = wikiPageName("AllPagesTest");
    editWikiPage(name, "Should appear in all pages", true);

    HtmlPage allPages = getWebPage("pages/AllPages");
    assertTrue(allPages.getTitleText().endsWith("All Pages"));
    HtmlAnchor link = allPages.getAnchorByHref(name);
    assertEquals(name, link.asText());
  }
  
  /**
   * Create two new pages.
   * Ensure they appear at the top of RecentChanges with the second page first.
   * Edit the first page.
   * Ensure it it now at the top of recent changes.
   */
  public void testRecentChanges() throws Exception {
    String createdFirst = wikiPageName("RecentChangesTestFirst");
    editWikiPage(createdFirst, "", true);
    String createdSecond = wikiPageName("RecentChangesTestSecond");
    editWikiPage(createdSecond, "", true);
    
    Iterator<HtmlAnchor> links = getRecentChangesLinks();
    HtmlAnchor first = links.next();
    HtmlAnchor second = links.next();
    assertEquals(first.asText(), createdSecond);
    assertEquals(second.asText(), createdFirst);
    
    editWikiPage(createdFirst, "", false);
    links = getRecentChangesLinks();
    first = links.next();
    second = links.next();
    assertEquals(first.asText(), createdFirst);
    assertEquals(second.asText(), createdSecond);
  }

  @SuppressWarnings("unchecked")
  private Iterator<HtmlAnchor> getRecentChangesLinks() throws IOException, JaxenException {
    HtmlPage recentChanges = getWebPage("pages/RecentChanges");
    Iterator<HtmlAnchor> links = recentChanges.getByXPath("//tr/td[position() = 2]/a").iterator();
    return links;
  }
  
  /**
   * Search by WikiWord and Wiki Word.
   */
  public void testNewPageCanBeFoundBySearchIndex() throws Exception {
    String name = wikiPageName("SearchIndexTest");
    HtmlPage page = editWikiPage(name, "Should be found by search", true);
    assertSearchFindsPageUsingQuery(page, name, "found by search");
    assertSearchFindsPageUsingQuery(page, name, WikiWordUtils.pathToTitle(name).toString());
    HtmlPage searchResult = search(page, name);
    // Goes directly to the page.
    assertEquals(page.getWebResponse().getUrl(), searchResult.getWebResponse().getUrl());
  }

  private HtmlPage search(final HtmlPage page, final String query) throws IOException {
    HtmlForm searchForm = page.getFormByName("searchForm");
    searchForm.getInputByName("query").setValueAttribute(query);
    HtmlPage results = (HtmlPage) searchForm.getInputByValue("Go").click();
    return results;
  }

  private void assertSearchFindsPageUsingQuery(final HtmlPage page, final String name, final String query) throws IOException {
    HtmlPage results = search(page, query);
    results.getAnchorByHref("/svnwiki/pages/" + name);
  }
  
}
