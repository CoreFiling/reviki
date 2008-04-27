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

import junit.framework.TestCase;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.urls.ApplicationUrls;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Test for {@link WikiUrlsImpl}.
 * 
 * @author mth
 */
public class TestWikiUrlsImpl extends TestCase {

  private WikiConfiguration _configuration;
  private ApplicationUrls _applicationUrls;

  @Override
  protected void setUp() throws Exception {
    _configuration = createMock(WikiConfiguration.class);
    _applicationUrls = createMock(ApplicationUrls.class);
    expect(_applicationUrls.url((String) EasyMock.anyObject())).andAnswer(new IAnswer<String>() {
      public String answer() {
        String relative = (String) EasyMock.getCurrentArguments()[0];
        return "http://www.example.com/reviki" + relative;
      }
    }).anyTimes();
  }

  public void testNullWiki() {
    WikiUrlsImpl urls = createURLs("foo", null);
    assertEquals("http://www.example.com/reviki/pages/", urls.root());
    assertEquals("http://www.example.com/reviki/pages/Spaced+Out", urls.page("Spaced Out"));
    assertEquals("http://www.example.com/reviki/pages/RecentChanges/atom.xml", urls.feed());
    assertEquals("http://www.example.com/reviki/pages/FindPage", urls.search());
  }

  public void testGivenNameWiki() {
    WikiUrlsImpl urls = createURLs("foo", "foo");
    assertEquals("http://www.example.com/reviki/pages/foo/", urls.root());
    assertEquals("http://www.example.com/reviki/pages/foo/Spaced+Out", urls.page("Spaced Out"));
    assertEquals("http://www.example.com/reviki/pages/foo/RecentChanges/atom.xml", urls.feed());
    assertEquals("http://www.example.com/reviki/pages/foo/FindPage", urls.search());
  }
  
  private WikiUrlsImpl createURLs(final String actual, final String given) {
    expect(_configuration.getWikiName()).andReturn(actual).anyTimes();
    expect(_configuration.getGivenWikiName()).andReturn(given).anyTimes();
    replay(_configuration, _applicationUrls);
    return new WikiUrlsImpl(_applicationUrls, _configuration);
  }
  
}
