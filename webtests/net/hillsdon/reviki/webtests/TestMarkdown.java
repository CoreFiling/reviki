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

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestMarkdown extends WebTestSupport {

  public void testSelectsSyntaxFromAttributes() throws Exception {
    editWikiPage("ConfigAutoProperties", "* = reviki:syntax=markdown", "", "", false);

    try {
      String name = uniqueWikiPageName("EditPageTest");
      HtmlPage edited = editWikiPage(name, "= Some reviki", "", "", false, "reviki");
      assertEquals("Some reviki", edited.querySelector("#wiki-rendering h1").asText());
    }
    finally {
      editWikiPage("ConfigAutoProperties", "", "", "", false);
    }
  }

  public void testSyntaxUsesWikiDefault() throws Exception {
    editWikiPage("ConfigAutoProperties", "* = reviki:syntax=markdown", "", "", false);

    try {
      String name = uniqueWikiPageName("EditPageTest");
      HtmlPage edited = editWikiPage(name, "# Some markdown", "", "", false);
      assertEquals("Some markdown", edited.querySelector("#wiki-rendering h1").asText());
    }
    finally {
      editWikiPage("ConfigAutoProperties", "", "", "", false);
    }
  }

  public void testSyntaxDefaultsToReviki() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    HtmlPage edited = editWikiPage(name, "# Some reviki", "", "", false);
    assertEquals("Some reviki", edited.querySelector("#wiki-rendering li").asText());
  }

  public void testUnsavedSpecialPagesUseCorrectSyntax() throws Exception {
    editWikiPage("ConfigAutoProperties", "* = reviki:syntax=markdown", "", "", false);

    try {
      // Check for incorrectly rendered markdown or reviki
      assertFalse(getWikiPage("ConfigSideBar").asText().contains("\\"));
      assertFalse(getWikiPage("ConfigSideBar").asText().contains("[ConfigSideBar](ConfigSideBar)"));
      assertFalse(getWikiPage("FrontPage").asText().contains("//reviki//"));
      assertFalse(getWikiPage("FrontPage").asText().contains("_reviki_"));

      // Check we render some appropriate HTML
      assertEquals(3, getWikiPage("ConfigSideBar").querySelectorAll("#wiki-rendering a").size());
      assertEquals(1, getWikiPage("FrontPage").querySelectorAll("#wiki-rendering em").size());
    }
    finally {
      editWikiPage("ConfigAutoProperties", "", "", "", false);
    }
  }

}
