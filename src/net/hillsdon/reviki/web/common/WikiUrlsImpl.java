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
package net.hillsdon.reviki.web.common;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.configuration.ApplicationUrls;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.wiki.WikiUrls;

/**
 * We should not base the URLs on the request.
 * 
 * We need to know the public base URL anyway really for e.g. atom ids
 * (we may be proxied through e.g. Apache).
 * 
 * @author mth
 */
public class WikiUrlsImpl implements WikiUrls {

  private final ApplicationUrls _applicationUrls;
  private final String _givenWikiName;

  /**
   * For DI.
   */
  public WikiUrlsImpl(final ApplicationUrls applicationUrls, final WikiConfiguration configuration) {
    this(applicationUrls, configuration.getGivenWikiName());
  }
  
  public WikiUrlsImpl(final ApplicationUrls applicationUrls, final String givenWikiName) {
    _applicationUrls = applicationUrls;
    _givenWikiName = givenWikiName;
  }

  public String root() {
    String relative = "/pages/";
    if (_givenWikiName != null) {
      relative += Escape.url(_givenWikiName) + "/";
    }
    return _applicationUrls.url(relative);
  }
  
  public String page(final String name) {
    return root() + Escape.url(name);
  }

  public String search() {
    return page("FindPage");
  }

  public String feed() {
    return page("RecentChanges") + "/atom.xml";
  }

  public String favicon() {
    return _applicationUrls.url("/resources/favicon.ico");
  }

}
