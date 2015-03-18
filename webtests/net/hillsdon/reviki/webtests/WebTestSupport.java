/**
 * Copyright 2008 Matthew Hillsdon
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.httpclient.util.URIUtil;
import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Superclass for writing HtmlUnit tests for the wiki.
 *
 * @author mth
 */
public abstract class WebTestSupport extends TestCase {

  public static final String BASE_URL = System.getProperty("wiki.url", "http://localhost:8980/reviki");

  public static final String NEWLINE_TEXTAREA = "\n";

  private WebClient _client;
  private WebClient _altclient = null;

  private WebClient setupClient(final String username, final String password) {
    final WebClient client = new WebClient(BrowserVersion.FIREFOX_24);
    final WebClientOptions options = client.getOptions();
    DefaultCredentialsProvider credentials = new DefaultCredentialsProvider();
    credentials.addCredentials(username, password);
    client.setCredentialsProvider(credentials);
    options.setRedirectEnabled(true);
    options.setThrowExceptionOnFailingStatusCode(true);
    client.addWebWindowListener(new ValidateOnContentChange());
    client.getCookieManager().setCookiesEnabled(false);

    // Try to log only "interesting" things:
    // Don't log errors we can't fix due to browser bugs etc.
    client.setIncorrectnessListener(new SuppressingIncorrectnessListener());

    return client;
  }

  @Override
  protected void setUp() throws Exception {
    _client = setupClient(getUsername(), getPassword());
  }

  protected void ignoreStatusCodeErrors() {
    _client.getOptions().setThrowExceptionOnFailingStatusCode(false);
  }

  protected void switchUser() {
    if (_altclient == null) {
      _altclient = setupClient(getAltUsername(), getAltPassword());
    }
    final WebClient temp = _client;
    _client = _altclient;
    _altclient = temp;
  }

  protected WebClient getClient() {
    return _client;
  }

  protected boolean isEditPage(final HtmlPage page) {
    return page.getByXPath("input[@name='save']").size() == 1;
  }

  protected boolean hasErrorMessage(final HtmlPage page) throws Exception {
    try {
      return page.getByXPath("id('flash')").size() > 0;
    }
    catch (ElementNotFoundException e) {
      return false;
    }
  }

  protected String getErrorMessage(final HtmlPage page) throws Exception {
    return ((DomText) page.getByXPath("id('flash')/div/text()").iterator().next()).asText().trim();
  }

  protected String getAltUsername() {
    return System.getProperty("wiki.altusername");
  }

  protected String getAltPassword() {
    return System.getProperty("wiki.altpassword");
  }

  protected String getUsername() {
    return System.getProperty("wiki.username");
  }

  protected String getPassword() {
    return System.getProperty("wiki.password");
  }

  protected String getSvnLocation() {
    return System.getProperty("wiki.svn");
  }

  private String getUrl(final String path) {
    return BASE_URL + "/" + path;
  }

  protected HtmlPage getWebPage(final String path) throws IOException {
    return (HtmlPage) _client.getPage(getUrl(path));
  }
  
  protected XmlPage getXmlPage(final String path) throws IOException {
    return (XmlPage) _client.getPage(getUrl(path));
  }

  /**
   * @param name Name of page.
   * @return That page in the 'test' wiki.
   * @throws IOException On error.
   */
  protected HtmlPage getWikiPage(final String name) throws IOException {
    return getWebPage("pages/test/" + URIUtil.encodeWithinPath(name));
  }
  
  protected XmlPage getHistoryAtomFeed(final String name) throws IOException {
    return getXmlPage("pages/test/" + URIUtil.encodeWithinPath(name) + "?history&ctype=atom");
  }

  private int _counter = 0;
  protected String uniqueWikiPageName(final String prefix) {
    return prefix + uniqueName();
  }

  protected String uniqueName() {
    return "" + System.currentTimeMillis() + _counter++;
  }

  protected HtmlPage renamePage(final String pageFrom, final String pageTo) throws IOException {
    HtmlPage page = getWikiPage(pageFrom);
    HtmlAnchor renameAnchor = page.getAnchorByName("rename");
    page = (HtmlPage) renameAnchor.click();
    final HtmlForm form = page.getFormByName("renameForm");
    final HtmlInput input = form.getInputByName("toPage");
    input.setValueAttribute(pageTo);
    page = (HtmlPage) (form.getButtonByName("rename")).click();
    assertEquals(1, page.getByXPath("id('wiki-rendering')").size());
    return page;
  }

  /**
   * @param name Page name, a WikiWord.
   * @param content Content to set.
   * @param attributes Attributes to set.
   * @param descriptionOfChange Description of the change.
   * @param isNew Used to assert the page is either new or existing.
   * @return The page after the 'Save' button has been clicked.
   * @throws IOException On failure.
   */
  public HtmlPage editWikiPage(final String name, final String content, final String attributes, final String descriptionOfChange, final Boolean isNew) throws Exception {
    return editWikiPage(getWikiPage(name), content, attributes, descriptionOfChange, isNew);
  }

  private void assertMatches(final String re, final String text) {
    assertTrue(text, text.matches(re));
  }

  protected HtmlPage editWikiPage(/* mutable */ HtmlPage page, final String content, final String attributes, final String descriptionOfChange, final Boolean isNew) throws Exception {
    URL pageUrl = page.getWebResponse().getWebRequest().getUrl();
    final String newSign = isNew != null && isNew ? " - New" : "";
    if (isNew != null) {
      assertMatches("test - [A-Z].*?" + newSign, page.getTitleText());
    }
    page = clickEditLink(page);
    if (isNew != null) {
      assertMatches("test - [*][A-Z].*?" + newSign, page.getTitleText());
    }
    HtmlForm editForm = page.getFormByName("editForm");
    editForm.getTextAreaByName("content").setText(content == null ? "" : content);
    editForm.getTextAreaByName("attributes").setText(attributes == null ? "" : attributes);
    editForm.getInputByName("description").setValueAttribute(descriptionOfChange == null ? "" : descriptionOfChange);
    page = (HtmlPage) editForm.getButtonByName("save").click();

    @SuppressWarnings("unchecked")
    final List<HtmlInput> saveButtons = (List<HtmlInput>) page.getByXPath("//input[@type='submit' and @value='Save']");
    assertEquals(0, saveButtons.size());

    assertURL(pageUrl, page.getWebResponse().getWebRequest().getUrl());
    return page;
  }

  protected HtmlAnchor getAnchorByHrefContains(final HtmlPage page, final String hrefContains) throws JaxenException {
    return (HtmlAnchor) page.getByXPath("//a[contains(@href, '" + hrefContains + "')]").iterator().next();
  }

  protected String removeSessionId(final String url) {
    // The first caters for Tomcat, the second Jetty.
    return url.replaceFirst("[;][^?]*", "").replaceFirst("%3B[^?]*", "");
  }

  protected void assertURL(final String expected, final String actual) {
    assertEquals(removeSessionId(expected.toString()), removeSessionId(actual.toString()));
  }

  protected void assertURL(final URL expected, final URL actual) {
    assertURL(expected.toString(), actual.toString());
  }

  protected HtmlPage clickEditLink(final HtmlPage page) throws IOException {
    HtmlForm editForm = page.getFormByName("editTop");
    HtmlPage editPage = (HtmlPage) editForm.getInputByValue("Edit").click();
    return editPage;
  }

  private static final Pattern RE_REVISION = Pattern.compile("r[0-9]+");

  protected long getRevisionNumberFromTitle(final HtmlPage page) {
    Matcher matcher = RE_REVISION.matcher(page.getTitleText());
    assertTrue(matcher.find());
    long revision = Long.parseLong(matcher.group().substring(1));
    return revision;
  }

  public static HtmlPage clickAttachmentsLink(final HtmlPage page, final String name) throws IOException {
    HtmlAnchor attachmentsLink = page.getAnchorByName("attachments");
    assertEquals("Attachments", attachmentsLink.asText());
    HtmlPage attachments = (HtmlPage) attachmentsLink.click();
    return attachments;
  }

  public HtmlPage uploadAttachment(final String fileName, final String pageName) throws IOException {
    return uploadAttachment(fileName, pageName, "");
  }

  public HtmlPage uploadAttachment(final String fileName, final String pageName, final String message) throws IOException {
    return uploadAttachment(fileName, pageName, "file.txt", message);
  }

  public HtmlPage uploadAttachment(final String fileName, final String pageName, final String uploadFileName, final String message) throws IOException {
    HtmlPage attachments = clickAttachmentsLink(getWikiPage(pageName), pageName);
    HtmlForm form = attachments.getFormByName("attachmentUpload");
    form.getInputByName("file").setValueAttribute(fileName);
    form.getInputByName("attachmentName").setValueAttribute(uploadFileName);
    form.getInputByName("attachmentMessage").setValueAttribute(message);
    attachments = (HtmlPage) form.getButtonByName("upload").click();
    return attachments;
  }

  protected HtmlPage search(final HtmlPage page, final String query) throws IOException {
    HtmlForm searchForm = page.getFormByName("searchForm");
    searchForm.getInputByName("query").setValueAttribute(query);
    HtmlPage results = (HtmlPage) searchForm.getInputByValue("Go").click();
    return results;
  }

  protected void assertSearchFindsPageUsingQuery(final HtmlPage page, final String name, final String query) throws IOException, JaxenException {
    HtmlPage results = search(page, query);
    getAnchorByHrefContains(results, BASE_URL + "/pages/test/" + name);
  }

  protected void assertSearchDoesNotFindPage(final HtmlPage start, final String pageName) throws IOException {
    assertTrue(search(start, pageName).asText().contains("No results found"));
  }

  protected HtmlPage getWikiList() throws IOException {
    return getWebPage("list");
  }

}
