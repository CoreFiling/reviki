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
package net.hillsdon.reviki.web.vcintegration;

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_FRONT_PAGE;
import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;

public class TestSpecialPagePopulatingPageStore extends TestCase {

  private SimplePageStore _delegate;
  private SpecialPagePopulatingPageStore _special;

  @Override
  protected void setUp() throws Exception {
    _delegate = new SimplePageStore();
    _special = new SpecialPagePopulatingPageStore(_delegate);
  }
  
  public void testAddsSpecialPagesToList() throws Exception {
    assertTrue(_special.list().contains(new PageReferenceImpl("ConfigSvnLocation")));
  }
  
  public void testPopulatesSomePages() throws Exception {
    PageInfo frontPage = _special.get(PAGE_FRONT_PAGE, -1);
    assertTrue(frontPage.isNew());
    assertEquals(PageInfo.UNCOMMITTED, frontPage.getLastChangedRevision());
    assertTrue(frontPage.getContent().contains("Welcome to"));
  }
  
  public void testOnlyPopulatedThePageIfTheUnderlyingStoreDoesntHaveIt() throws Exception {
    _delegate.set(PAGE_FRONT_PAGE, "", -1, "foo", "an edit");
    assertEquals("foo", _special.get(PAGE_FRONT_PAGE, -1).getContent());
  }
  
}
