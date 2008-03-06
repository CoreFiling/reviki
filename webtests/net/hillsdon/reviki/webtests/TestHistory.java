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
    assertEquals(pageName, altered.getCell(1).asText());
    assertEquals(getUsername(), altered.getCell(2).asText());
    assertEquals("s/Initial/Altered", altered.getCell(3).asText());
    HtmlTableRow initial = historyRows.get(1);
    assertEquals(pageName, initial.getCell(1).asText());
    assertEquals(getUsername(), initial.getCell(2).asText());
    assertEquals("None", initial.getCell(3).asText());

    HtmlAnchor diffLink = (HtmlAnchor) altered.getCell(3).getByXPath("a").iterator().next();
    HtmlPage diff = (HtmlPage) diffLink.click();
    assertEquals("Altered", ((DomNode) diff.getByXPath("//ins").iterator().next()).asText());
    assertEquals("Initial", ((DomNode) diff.getByXPath("//del").iterator().next()).asText());
  }
  
}
