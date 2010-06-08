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

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.WikiUrls;

/**
 * Common super-class without base URL knowledge.
 * 
 * @author mth
 */
public abstract class AbstractWikiUrls implements WikiUrls {

  public final String page(final String wikiName, final String name, final URLOutputFilter urlOutputFilter) {
    return page(wikiName, name, "", urlOutputFilter);
  }

  public String page(final String wikiName, final String pageName, final String extraUnescaped, final URLOutputFilter urlOutputFilter) {
    return urlOutputFilter.filterURL(pagesRoot(wikiName) + Escape.urlEncodeUTF8(pageName) + extraUnescaped);
  }
  
  public final String search(final URLOutputFilter urlOutputFilter) {
    return search(null, urlOutputFilter);
  }

  public final String search(final String wikiName, final URLOutputFilter urlOutputFilter) {
    return page(wikiName, "FindPage", urlOutputFilter);
  }
  
  public final String feed(final URLOutputFilter urlOutputFilter) {
    return feed(null, urlOutputFilter);
  }

  public final String feed(final String wikiName, final URLOutputFilter urlOutputFilter) {
    return page(wikiName, "RecentChanges", "?ctype=atom", urlOutputFilter);
  }
  
  public final String resource(final String path) {
    return resource(null, path);
  }

  public final String resource(final String wikiName, final String path) {
    return pagesRoot(wikiName) + "resources/" + path;
  }

}
