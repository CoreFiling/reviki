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
package net.hillsdon.reviki.configuration;

import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;

public class TestPageStoreConfiguration extends TestCase {

  private SimplePageStore _store;
  private PageStoreConfiguration _configuration;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore();
    _configuration = new PageStoreConfiguration(_store);
  }

  private void assertNoInterWikiLinks() throws PageStoreException {
    assertTrue(_configuration.getInterWikiLinker().getWikiToFormatStringMap().isEmpty());
  }
  
  public void testInterWikiLinkerEmptyWhenNoPage() throws Exception {
    assertNoInterWikiLinks();
  }
  
  public void testAddingPagePopulatesInterWikiLinker() throws Exception  {
    _store.set(new PageReference("ConfigInterWikiLinks"), "", -1, "c2 http://c2.com/cgi/wiki?%s\r\n", "");
    assertEquals(Collections.singletonMap("c2", "http://c2.com/cgi/wiki?%s"), _configuration.getInterWikiLinker().getWikiToFormatStringMap());
  }
  
  // Currently most things are considered valid, we split on first whitespace...
  public void testInvalidEntryIgnored() throws Exception {
    _store.set(new PageReference("ConfigInterWikiLinks"), "", -1, "nospace\r\n", "");
    assertNoInterWikiLinks();
  }
  
}
