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

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;


public class TestHistory extends WebTestSupport {

  @SuppressWarnings("unchecked")
  public void test() throws Exception {
    String pageName = uniqueWikiPageName("HistoryTest");
    HtmlPage page = editWikiPage(pageName, "Initial content", "", true);
    page = editWikiPage(pageName, "Altered content", "s/Initial/Altered", false);
    HtmlPage history = (HtmlPage) ((HtmlAnchor) page.getByXPath("//a[@name='history']").iterator().next()).click();
    List<HtmlTableRow> historyRows = history.getByXPath("//tr[td]");
    assertEquals(3, historyRows.size());
    HtmlTableRow altered = historyRows.get(1);
    // First column is date/time.
    verifyRow(altered, "s/Initial/Altered");
    HtmlTableRow initial = historyRows.get(2);
    verifyRow(initial, "None");

    final List<HtmlSubmitInput> compareButtons = (List<HtmlSubmitInput>) history.getByXPath("//input[@type='submit' and @value='Compare']/.");
    assertEquals(1, compareButtons.size());
    HtmlPage diff = (HtmlPage) compareButtons.get(0).click();
    assertEquals("Altered", ((DomNode) diff.getByXPath("//ins").iterator().next()).asText());
    assertEquals("Initial", ((DomNode) diff.getByXPath("//del").iterator().next()).asText());
    List<HtmlDivision> divs = diff.getByXPath("//div[@id='flash']/.");
    assertEquals(0, divs.size());

    // Check for the warning when viewing differences backwards
    final List<HtmlRadioButtonInput> radioButtons = history.getByXPath("//input[@type='radio']/.");
    assertEquals(4, radioButtons.size());
    radioButtons.get(0).click();
    radioButtons.get(3).click();
    diff = (HtmlPage) compareButtons.get(0).click();
    divs = diff.getByXPath("//div[@id='flash']/.");
    assertEquals(1, divs.size());

  }

  private void verifyRow(HtmlTableRow altered, String content) {
    assertEquals(getUsername(), altered.getCell(2).asText());
    assertEquals(content, altered.getCell(3).asText());
  }
  
}
