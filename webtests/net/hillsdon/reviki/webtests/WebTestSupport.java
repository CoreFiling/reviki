/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.webtests;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Superclass for writing HtmlUnit tests for the wiki.
 * 
 * @author mth
 */
public abstract class WebTestSupport extends TestCase {

  private static final String BASE_URL = "http://localhost:8080/reviki";
  
  private WebClient _client;

  @Override
  protected void setUp() throws Exception {
    _client = new WebClient();
    DefaultCredentialsProvider credentials = new DefaultCredentialsProvider();
    credentials.addCredentials(getUsername(), System.getProperty("wiki.password"));
    _client.setCredentialsProvider(credentials);
    _client.setRedirectEnabled(true);
    _client.setThrowExceptionOnFailingStatusCode(true);
    _client.setThrowExceptionOnScriptError(true);
  }

  protected String getUsername() {
    return System.getProperty("wiki.user");
  }

  private String getUrl(final String path) {
    return BASE_URL + "/" + path;
  }

  protected HtmlPage getWebPage(final String path) throws IOException {
    return (HtmlPage) _client.getPage(getUrl(path));
  }
  
  /**
   * @param name Name of page.
   * @return That page in the 'test' wiki.
   * @throws IOException On error.
   */
  protected HtmlPage getWikiPage(final String name) throws IOException {
    return getWebPage("pages/test/" + name);
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
  public HtmlPage editWikiPage(final String name, final String content, final String descriptionOfChange, final Boolean isNew) throws IOException {
    HtmlPage page = getWikiPage(name);
    URL pageUrl = page.getWebResponse().getUrl();
    if (isNew != null) {
      assertTrue(!isNew ^ page.getTitleText().endsWith(" - New"));
    }
    page = clickEditLink(page);
    HtmlForm editForm = page.getFormByName("editForm");
    editForm.getTextAreaByName("content").setText(content == null ? "" : content);
    editForm.getInputByName("description").setValueAttribute(descriptionOfChange == null ? "" : descriptionOfChange);
    page = (HtmlPage) editForm.getInputByValue("Save").click();
    assertEquals(pageUrl, page.getWebResponse().getUrl());
    return page;
  }

  protected HtmlPage clickEditLink(final HtmlPage page) throws IOException {
    return (HtmlPage) page.getAnchorByName("editTopSubmitLink").click();
  }
  
  private static final Pattern RE_REVISION = Pattern.compile("r[0-9]+");
  
  protected long getRevisionNumberFromTitle(final HtmlPage page) {
    Matcher matcher = RE_REVISION.matcher(page.getTitleText());
    assertTrue(matcher.find());
    long revision = Long.parseLong(matcher.group().substring(1));
    return revision;
  }

  public static HtmlPage clickAttachmentsLink(final HtmlPage page, final String name) throws IOException {
    HtmlAnchor attachmentsLink = page.getAnchorByHref(name + "/attachments/");
    assertEquals("Attachments", attachmentsLink.asText());
    HtmlPage attachments = (HtmlPage) attachmentsLink.click();
    return attachments;
  }

  public HtmlPage uploadAttachment(final String fileName, final String pageName) throws IOException {
    HtmlPage attachments = clickAttachmentsLink(getWikiPage(pageName), pageName);
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    form.getInputByName("file").setValueAttribute(fileName);
    form.getInputByName("attachmentName").setValueAttribute("file");
    attachments = (HtmlPage) form.getInputByValue("Upload").click();
    return attachments;
    
  }
  
  protected HtmlPage search(final HtmlPage page, final String query) throws IOException {
    HtmlForm searchForm = page.getFormByName("searchForm");
    searchForm.getInputByName("query").setValueAttribute(query);
    HtmlPage results = (HtmlPage) searchForm.getInputByValue("Go").click();
    return results;
  }
  
  protected void assertSearchFindsPageUsingQuery(final HtmlPage page, final String name, final String query) throws IOException {
    HtmlPage results = search(page, query);
    results.getAnchorByHref("/reviki/pages/test/" + name);
  }

  protected void assertSearchDoesNotFindPage(HtmlPage start, String pageName) throws IOException {
    assertTrue(search(start, pageName).asText().contains("No results found"));
  }

}
