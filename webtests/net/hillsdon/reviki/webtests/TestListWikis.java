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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class TestListWikis extends WebTestSupport {

  private HtmlPage getWikiList() throws IOException {
    return getWebPage("list");
  }

  public void testWikiListContainsTestWiki() throws Exception {
    HtmlPage list = getWikiList();
    boolean testWikiFound = false;
    for (Object o : list.getByXPath("id('wikiList')/li")) {
      HtmlListItem li = (HtmlListItem) o;
      String href = ((HtmlAnchor) li.getByXPath("a").get(0)).getHrefAttribute();
      testWikiFound |= href.contains("pages/test/FrontPage");
    }
    assertTrue(testWikiFound);
  }

  public void testLinkToWiki() throws Exception {
    HtmlPage list = getWikiList();
    HtmlForm form = list.getFormByName("jump");
    form.getInputByName("name").setValueAttribute("foo");
    HtmlPage jumpedTo = (HtmlPage) form.getInputByName("go").click();
    assertTrue(jumpedTo.getTitleText().startsWith("foo - Config Svn Location"));
  }
  
}
