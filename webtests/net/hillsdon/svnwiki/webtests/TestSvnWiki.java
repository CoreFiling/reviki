package net.hillsdon.svnwiki.webtests;

import junit.framework.TestCase;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
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
  
  public void testRootRedirectsToFrontPage() throws Exception {
    final HtmlPage page = (HtmlPage) _client.getPage(getUrl(""));
    assertTrue(page.getTitleText().contains("Front Page"));
  }
  
  /**
   * Create a page.
   * Ensure it appears in AllPages.
   */
  public void testAllPages() {
  }
  
  /**
   * Create two new pages.
   * Ensure they appear at the top of RecentChanges with the second page first.
   */
  public void testRecentChanges() {
  }
  
  /**
   * Search by WikiWord and Wiki Word.
   */
  public void testNewPageCanBeFoundBySearchIndex() {
  }
  
}
