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
package net.hillsdon.reviki.web.urls.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class TestPageStoreConfiguration extends TestCase {

  private SimplePageStore _store;
  private PageStoreConfiguration _configuration;
  private ApplicationUrls _applicationUrls;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore();
    _applicationUrls = createMock(ApplicationUrls.class);
    WikiUrls wikiUrls = createMock(WikiUrls.class);
    expect(wikiUrls.getWikiName()).andReturn("other");
    expect(wikiUrls.interWikiTemplate()).andStubReturn(".../other/%s");
    expect(_applicationUrls.getAvailableWikiUrls()).andStubReturn(ImmutableSet.of(wikiUrls));
    replay(_applicationUrls, wikiUrls);
    _configuration = new PageStoreConfiguration(_store, _applicationUrls);
  }

  private void assertOnlyBuiltInWikiLinks() throws PageStoreException {
    Map<String, String> expected = ImmutableMap.of("other", ".../other/%s");
    assertEquals(expected, _configuration.getInterWikiLinker().getWikiToFormatStringMap());
  }

  public void testInterWikiLinkerEmptyWhenNoPage() throws Exception {
    assertOnlyBuiltInWikiLinks();
  }

  public void testAddingPagePopulatesInterWikiLinker() throws Exception  {
    _store.set(new PageInfoImpl(null, "ConfigInterWikiLinks", "c2 http://c2.com/cgi/wiki?%s\r\n", Collections.<String, String>emptyMap()), "", -1, "");
    Map<String, String> expected = ImmutableMap.of(
        "other", ".../other/%s",
        "c2", "http://c2.com/cgi/wiki?%s"
    );
    assertEquals(expected, _configuration.getInterWikiLinker().getWikiToFormatStringMap());
  }

  public void testUserEntryWinsOverBuiltInEntry() throws Exception {
    _store.set(new PageInfoImpl(null, "ConfigInterWikiLinks", "other http://www.example.com/elsewhere/%s\r\n", Collections.<String, String>emptyMap()), "", -1, "");
    Map<String, String> expected = ImmutableMap.of(
        "other", "http://www.example.com/elsewhere/%s"
    );
    assertEquals(expected, _configuration.getInterWikiLinker().getWikiToFormatStringMap());
  }

  // Currently most things are considered valid, we split on first whitespace...
  public void testInvalidEntryIgnored() throws Exception {
    _store.set(new PageInfoImpl(null, "ConfigInterWikiLinks", "nospace\r\n", Collections.<String, String>emptyMap()), "", -1, "");
    assertOnlyBuiltInWikiLinks();
  }

}
