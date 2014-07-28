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
package net.hillsdon.reviki.wiki;

import java.util.Collections;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import junit.framework.TestCase;

public class TestRenderedPage extends TestCase {

  private RenderedPageFactory _factory;

  @Override
  protected void setUp() throws Exception {
    _factory = new RenderedPageFactory(MarkupRenderer.AS_IS);
  }

  private RenderedPage create(String content) throws Exception {
    return _factory.create(new PageInfoImpl("wiki", "FrontPage", content, Collections.<String, String>emptyMap()), URLOutputFilter.NULL);
  }

  public void testGetPage() throws Exception {
    RenderedPage rendered = create("");
    rendered.getPage().equals("FrontPage");
  }
}
