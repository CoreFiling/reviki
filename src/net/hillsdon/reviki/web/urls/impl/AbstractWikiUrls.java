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
import net.hillsdon.reviki.web.urls.WikiUrls;

/**
 * Common super-class without base URL knowledge.
 * 
 * @author mth
 */
public abstract class AbstractWikiUrls implements WikiUrls {

  protected abstract String url(String relative);
  
  public final String page(final String name) {
    return pagesRoot() + Escape.url(name);
  }

  public final String search() {
    return page("FindPage");
  }

  public final String feed() {
    return page("RecentChanges") + "/atom.xml";
  }

  public final String resource(final String path) {
    return pagesRoot() + "resources/" + path;
  }

}
