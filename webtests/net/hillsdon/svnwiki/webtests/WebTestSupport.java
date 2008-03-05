package net.hillsdon.svnwiki.webtests;

import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Superclass for writing HtmlUnit tests for the wiki.
 * 
 * @author mth
 */
public abstract class WebTestSupport extends TestCase {

  private static final String BASE_URL = "http://localhost:8080/svnwiki";
  
  private WebClient _client;

  @Override
  protected void setUp() throws Exception {
    _client = new WebClient();
    DefaultCredentialsProvider credentials = new DefaultCredentialsProvider();
    credentials.addCredentials(System.getProperty("wiki.user"), System.getProperty("wiki.password"));
    _client.setCredentialsProvider(credentials);
    _client.setRedirectEnabled(true);
    _client.setThrowExceptionOnFailingStatusCode(true);
    _client.setThrowExceptionOnScriptError(true);
  }

  protected String getUrl(final String path) {
    return BASE_URL + "/" + path;
  }

  protected HtmlPage getWebPage(final String path) throws IOException {
    return (HtmlPage) _client.getPage(getUrl(path));
  }

  private int _counter = 0;
  protected String uniqueWikiPageName(final String prefix) {
    return prefix + System.currentTimeMillis() + _counter++;
  }

  /**
   * @param name Page name, a WikiWord.
   * @param content Content to set.
   * @param descriptionOfChange Description of the change.
   * @param isNew Used to assert the page is either new or existing.
   * @return The page after the 'Save' button has been clicked.
   * @throws IOException On failure.
   */
  public HtmlPage editWikiPage(final String name, final String content, final String descriptionOfChange, final boolean isNew) throws IOException {
    HtmlPage page = getWebPage("pages/" + name);
    URL pageUrl = page.getWebResponse().getUrl();
    assertTrue(!isNew ^ page.getTitleText().endsWith(" - New"));
    page = (HtmlPage) page.getFormByName("editForm").getInputByValue("Edit").click();
    HtmlForm editForm = page.getFormByName("editForm");
    editForm.getTextAreaByName("content").setText(content == null ? "" : content);
    editForm.getInputByName("description").setValueAttribute(descriptionOfChange == null ? "" : descriptionOfChange);
    page = (HtmlPage) editForm.getInputByValue("Save").click();
    assertEquals(pageUrl, page.getWebResponse().getUrl());
    // Only holds true if there's no mark-up...
    page.getWebResponse().getContentAsString().contains(content);
    return page;
  }

}
