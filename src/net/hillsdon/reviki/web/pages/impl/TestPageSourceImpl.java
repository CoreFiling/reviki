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
package net.hillsdon.reviki.web.pages.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import junit.framework.TestCase;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.pages.PageSource;
import net.hillsdon.reviki.web.pages.SpecialPage;
import net.hillsdon.reviki.web.pages.SpecialPages;

public class TestPageSourceImpl extends TestCase {
  
  private SpecialPages _specialPages;
  private DefaultPage _defaultPage;

  private PageSource _pageSource;

  @Override
  protected void setUp() throws Exception {
    _specialPages = createMock(SpecialPages.class);
    _defaultPage = createMock(DefaultPage.class);
    _pageSource = new PageSourceImpl(_specialPages, _defaultPage);
  }
  
  public void testReturnsSpecialPageIfAvailable() {
    SpecialPage specialPage = createMock(SpecialPage.class);
    expect(_specialPages.get("TheSpecialPage")).andReturn(specialPage).atLeastOnce();
    replay(_specialPages, specialPage, _defaultPage);
    assertSame(specialPage, _pageSource.get(new PageReferenceImpl("TheSpecialPage")));
  }
  
  public void testFallsbackToDefaultPage() {
    expect(_specialPages.get("AnOrdinaryPage")).andReturn(null).atLeastOnce();
    replay(_specialPages, _defaultPage);
    assertSame(_defaultPage, _pageSource.get(new PageReferenceImpl("AnOrdinaryPage")));
  }
  
}
