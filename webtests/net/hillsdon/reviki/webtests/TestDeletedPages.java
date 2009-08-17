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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Deleted pages cause potential problems all over the place.
 * 
 * @author mth
 */
public class TestDeletedPages extends WebTestSupport {

  static class NamedPage {
    private final String _name;
    private final HtmlPage _page;
    public NamedPage(String name, HtmlPage page) {
      _name = name;
      _page = page;
    }
  }
  
  public void testClearTextDeletesPage() throws Exception {
    HtmlPage deleted = createThenDeletePage()._page;
    assertTrue(deleted.asText().contains("new page"));
  }

  public void testCanViewHistoryForDeletedPage() throws Exception {
    HtmlPage deleted = createThenDeletePage()._page;
    HtmlAnchor historyLink = deleted.getAnchorByName("history");
    // This used to give an error.
    historyLink.click();
  }
  
  public void testCanRecreateDeletedPage() throws Exception {
    NamedPage page = createThenDeletePage();
    String expectedContent = "The new content";
    assertTrue(editWikiPage(page._name, expectedContent, "Recreated", true).asText().contains(expectedContent));
  }

  public void testSearchDoesNotFindDeletedPage() throws Exception {
    NamedPage page = createThenDeletePage();
    assertSearchDoesNotFindPage(page._page, page._name);
  }

  public void testCanViewDeletedPage() throws Exception {
    final String content = "Distinctive content";
    final String name = uniqueWikiPageName("EditPageTest");
    
    HtmlPage original = editWikiPage(name, content, "", true);
    long originalRevision = getRevisionNumberFromTitle(original);
    editWikiPage(name, "", "Deleted", false);
    HtmlPage originalByRevision = getWebPage("pages/test/" + name + "?revision=" + originalRevision);
    assertTrue(originalByRevision.asText().contains(content));
  }
  
  private NamedPage createThenDeletePage() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    editWikiPage(name, "Initial content", "", true);
    HtmlPage page = editWikiPage(name, "", "", false);
    return new NamedPage(name, page);
  }
  
}
