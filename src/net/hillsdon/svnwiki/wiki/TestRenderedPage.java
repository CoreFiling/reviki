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
package net.hillsdon.svnwiki.wiki;

import static java.util.Arrays.asList;
import junit.framework.TestCase;

public class TestRenderedPage extends TestCase {

  private RenderedPageFactory _factory;

  @Override
  protected void setUp() throws Exception {
    _factory = new RenderedPageFactory(MarkupRenderer.AS_IS);
  }
  
  private RenderedPage create(String content) throws Exception {
    return _factory.create("FrontPage", content);
  }

  public void testGetPage() throws Exception {
    RenderedPage rendered = create("");
    rendered.getPage().equals("FrontPage");
  }
  
  public void testGetOutgoingLinksExistingPage() throws Exception {
    RenderedPage existingPage = create("<a href='pages/Foo'>Foo</a> to <a class='existing-page other-class' href='pages/Bar'>Bar description</a>");
    assertEquals(asList("Bar"), existingPage.findOutgoingWikiLinks());
  }

  public void testGetOutgoingLinksNewPage() throws Exception {
    RenderedPage newPage = create("<a href='pages/Foo'>Foo</a> to <a class='other-class new-page' href='pages/Bar'>Bar description</a>");
    assertEquals(asList("Bar"), newPage.findOutgoingWikiLinks());
  }
  
}
