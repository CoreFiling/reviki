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

import net.hillsdon.reviki.vc.PageReference;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.COMPLIMENTARY_CONTENT_PAGES;

public class TestMisc extends WebTestSupport {

  public void testWikiRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("pages/test/").getTitleText().contains("Front Page"));
    assertTrue(getWebPage("pages/test").getTitleText().contains("Front Page"));
  }
  
  public void testNoBackLinkToSelf() throws Exception {
    assertTrue(getWikiPage("FrontPage")
      .getByXPath("id('backlinks')//a[@href = 'FrontPage']").isEmpty());
  }
  
  public void testSidebarEtc() throws Exception {
    for (PageReference page : COMPLIMENTARY_CONTENT_PAGES) {
      final String expect = "T" + System.currentTimeMillis() + page.getPath().toLowerCase();
      editWikiPage(page.getPath(), expect, "Some new content", null);
      assertTrue(getWikiPage("FrontPage").asText().contains(expect));
      editWikiPage(page.getPath(), "", "Tidying", null);
    }
  }
  
  public void testConfigSvnLocationShowsFormToEditSvnLocation() throws Exception {
    getWikiPage("ConfigSvnLocation").getFormByName("configurationForm");
  }
  
}
