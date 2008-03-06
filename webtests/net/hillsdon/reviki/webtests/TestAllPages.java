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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class TestAllPages extends WebTestSupport {
  
  /**
   * Create a page.
   * Ensure it appears in AllPages.
   */
  public void testAllPages() throws Exception {
    String name = uniqueWikiPageName("AllPagesTest");
    editWikiPage(name, "Should appear in all pages", "", true);

    HtmlPage allPages = getWikiPage("AllPages");
    assertTrue(allPages.getTitleText().endsWith("All Pages"));
    HtmlAnchor link = allPages.getAnchorByHref("/svnwiki/pages/test/" + name);
    assertEquals(name, link.asText());
  }
  
}
