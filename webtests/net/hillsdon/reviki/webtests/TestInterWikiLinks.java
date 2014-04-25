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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class TestInterWikiLinks extends WebTestSupport {

  /**
   * Add an interwiki link mapping, create a link, make sure it points where we expect.
   */
  public void testLink() throws Exception {
    editWikiPage("ConfigInterWikiLinks", "foo http://www.example.com/Wiki?%25s", "", "Link mapping.", null);
    HtmlPage page = editWikiPage(uniqueWikiPageName("InterWikiLinkTest"), "foo:1234", "", "Add inter-wiki links.", true);
    HtmlAnchor link = page.getAnchorByHref("http://www.example.com/Wiki?1234");
    assertEquals("foo:1234", link.asText());
  }
  
  /**
   * Add many interiki links to a page and test the render time.
   */
  public void testRenderTiming() throws Exception {
    editWikiPage("ConfigInterWikiLinksTiming", "foo http://www.example.com/Wiki?%25s", "", "Link mapping.", null);
    String content = "";
    for (int i=0; i<100; i++) {
      content += "foo:" + i + " ";
    }
    String pageName = uniqueWikiPageName("ConfigInterWikiLinkTest");
    editWikiPage(pageName, content, "", "Add inter-wiki links.", true);

    assertTrue(wikiPageFetchTime(pageName, 3) < 1000);
  }

  /**
   * Fetch a wiki page several times and return the shortest elapsed time.
   */
  public long wikiPageFetchTime(String pageName, int iterations) throws IOException {
    long minElapsedTime = -1;

    for (int i=0; i<iterations; i++) {
      long startTime = System.currentTimeMillis();
      getWikiPage(pageName);
      long endTime = System.currentTimeMillis();

      long elapsedTime = endTime - startTime;
      if (minElapsedTime == -1 || elapsedTime < minElapsedTime) {
        minElapsedTime = elapsedTime;
      }
    }
    return minElapsedTime;
  }
}
