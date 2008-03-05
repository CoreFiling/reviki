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




public class TestMisc extends WebTestSupport {

  public void testAppRootRedirectsToWikiList() throws Exception {
    assertTrue(getWebPage("").getTitleText().contains("Wiki List"));
  }

  public void testWikiRootRedirectsToFrontPage() throws Exception {
    assertTrue(getWebPage("pages/test/").getTitleText().contains("Front Page"));
    assertTrue(getWebPage("pages/test").getTitleText().contains("Front Page"));
  }
  
  public void testNoBackLinkToSelf() throws Exception {
    assertTrue(getWebPage("pages/test/FrontPage")
      .getByXPath("id('backlinks')//a[@href = 'FrontPage']").isEmpty());
  }
  
}
