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

import java.io.IOException;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class TestEditing extends WebTestSupport {
  
  public void testEditPageIncrementsRevision() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    HtmlPage initial = editWikiPage(name, "Initial content", "", true);
    long initialRevision = getRevisionNumberFromTitle(initial);
    HtmlPage edited = editWikiPage(name, "Initial content.  Extra content.", "", false);
    assertEquals(initialRevision + 1, getRevisionNumberFromTitle(edited));
  }

  public void testCancelEditNewPage() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    editThenCancel(name);
  }
  
  public void testCancelEditExistingPage() throws Exception {
    String name = uniqueWikiPageName("EditPageTest");
    editWikiPage(name, "Whatever", "", true);
    editThenCancel(name);
  }

  private void editThenCancel(final String name) throws IOException {
    final String flagText = "Should not be saved.";
    HtmlPage editPage = clickEditLink((HtmlPage) getWebPage("pages/test/" + name));
    HtmlForm form = editPage.getFormByName("editForm");
    form.getTextAreaByName("content").setText(flagText);
    HtmlPage viewPage = (HtmlPage) form.getInputByValue("Cancel").click();
    assertFalse(viewPage.asText().contains(flagText));
  }
  
}
