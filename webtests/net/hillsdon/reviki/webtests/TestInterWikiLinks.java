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


public class TestInterWikiLinks extends WebTestSupport {

  /**
   * Add an interwiki link mapping, create a link, make sure it points where we expect.
   */
  public void test() throws Exception {
    editWikiPage("ConfigInterWikiLinks", "foo http://www.example.com/Wiki?%s", "Link mapping.", null);
    HtmlPage page = editWikiPage(uniqueWikiPageName("InterWikiLinkTest"), "foo:1234", "Add inter-wiki link.", true);
    HtmlAnchor link = page.getAnchorByHref("http://www.example.com/Wiki?1234");
    assertEquals("foo:1234", link.asText());
  }
  
}
