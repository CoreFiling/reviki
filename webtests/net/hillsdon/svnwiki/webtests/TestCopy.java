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
package net.hillsdon.svnwiki.webtests;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Tests copy.
 * 
 * @author mth
 */
public class TestCopy extends WebTestSupport {
  
  public void testCopyLinkNotAvailableForNonExistantPages() throws Exception {
    HtmlPage page = getWikiPage(uniqueWikiPageName("CopyLinkTest"));
    try {
      page.getAnchorByName("copy");
      fail();
    }
    catch (ElementNotFoundException expected) {
    }
  }
  
  public void testCopy() throws Exception {
    String fromPageName = uniqueWikiPageName("CopyTestFrom");
    String toPageName = uniqueWikiPageName("CopyTestTo");
    editWikiPage(fromPageName, "Catchy tunes", "Whatever", true);
    HtmlPage page = getWikiPage(fromPageName);
    page = (HtmlPage) page.getAnchorByName("copy").click();
    HtmlForm form = page.getFormByName("copyForm");
    form.getInputByName("toPage").setValueAttribute(toPageName);
    page = (HtmlPage) form.getInputByName("copy").click();
    assertTrue(page.getWebResponse().getUrl().toURI().getPath().endsWith(toPageName));
    assertTrue(page.asText().contains("Catchy tunes"));
    page = getWikiPage(fromPageName);
    assertTrue(page.asText().contains("Catchy tunes"));
  }
  
}
