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
import net.hillsdon.fij.text.Escape;

import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * Superclass for writing HtmlUnit tests for the wiki.
 *
 * @author mth
 */
public abstract class WebTestSupport extends TestCase {

  public static final String BASE_URL = "http://localhost:8080/reviki";
  public static final String NEWLINE_TEXTAREA = "\n";
  private WebClient _client;
  private WebClient _altclient = null;

  private WebClient setupClient(final String username, final String password) {
    final WebClient client = new WebClient();
    DefaultCredentialsProvider credentials = new DefaultCredentialsProvider();
    credentials.addCredentials(username, password);
    client.setCredentialsProvider(credentials);
    client.setRedirectEnabled(true);
    client.setThrowExceptionOnFailingStatusCode(true);
    client.setThrowExceptionOnScriptError(true);
    client.addWebWindowListener(new ValidateOnContentChange());
    client.getCookieManager().setCookiesEnabled(false);
    return client;
  }

  @Override
  protected void setUp() throws Exception {
    _client = setupClient(getUsername(), getPassword());
  }

  protected void ignoreStatusCodeErrors() {
    _client.setThrowExceptionOnFailingStatusCode(false);
  }

  /**
   * When retrieved by the webclient, newlines in textareas are just \n.
   * @return
   */
  protected String getNewlineTexarea() {
    return NEWLINE_TEXTAREA;
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
    return ((DomText) page.getByXPath("id('flash')/p/text()").iterator().next()).asText().trim();
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

  /**
   * @param name Name of page.
   * @return That page in the 'test' wiki.
   * @throws IOException On error.
   */
  protected HtmlPage getWikiPage(final String name) throws IOException {
    return getWebPage("pages/test/" + Escape.urlEncodeUTF8(name));
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
    page = (HtmlPage) ((HtmlSubmitInput) form.getInputByName("rename")).click();
    assertEquals(1, page.getByXPath("id('wiki-rendering')").size());
    return page;
  }

  /**
   * @param name Page name, a WikiWord.
   * @param content Content to set.
   * @param descriptionOfChange Description of the change.
   * @param isNew Used to assert the page is either new or existing.
   * @return The page after the 'Save' button has been clicked.
   * @throws IOException On failure.
   */
  public HtmlPage editWikiPage(final String name, final String content, final String descriptionOfChange, final Boolean isNew) throws Exception {
    return editWikiPage(getWikiPage(name), content, descriptionOfChange, isNew);
  }

  protected HtmlPage editWikiPage(/* mutable */ HtmlPage page, final String content, final String descriptionOfChange, final Boolean isNew) throws Exception {
    URL pageUrl = page.getWebResponse().getRequestUrl();
    if (isNew != null) {
      assertTrue(!isNew ^ page.getTitleText().endsWith(" - New"));
    }
    page = clickEditLink(page);
    HtmlForm editForm = page.getFormByName("editForm");
    editForm.getTextAreaByName("content").setText(content == null ? "" : content);
    editForm.getInputByName("description").setValueAttribute(descriptionOfChange == null ? "" : descriptionOfChange);
    page = (HtmlPage) editForm.getInputByValue("Save").click();

    @SuppressWarnings("unchecked")
    final List<HtmlInput> saveButtons = (List<HtmlInput>) page.getByXPath("//input[@type='submit' and @value='Save']");
    assertEquals(0, saveButtons.size());

    assertURL(pageUrl, page.getWebResponse().getRequestUrl());
    return page;
  }

  protected HtmlAnchor getAnchorByHrefContains(final HtmlPage page, final String hrefContains) throws JaxenException {
    return (HtmlAnchor) page.getByXPath("//a[contains(@href, '" + hrefContains + "')]").iterator().next();
  }

  protected String removeSessionId(final String url) {
    return url.replaceFirst("[;][^?]*", "");
  }

  protected void assertURL(final String expected, final String actual) {
    assertEquals(removeSessionId(expected.toString()), removeSessionId(actual.toString()));
  }

  protected void assertURL(final URL expected, final URL actual) {
    assertURL(expected.toString(), actual.toString());
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
    HtmlAnchor attachmentsLink = page.getAnchorByName("attachments");
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
