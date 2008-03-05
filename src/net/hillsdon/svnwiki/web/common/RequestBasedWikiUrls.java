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
package net.hillsdon.svnwiki.web.common;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.wiki.WikiUrls;

public class RequestBasedWikiUrls implements WikiUrls {

  private String _base;

  public RequestBasedWikiUrls(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    _base = requestURL.substring(0, requestURL.length() - path.length());
  }
  
  public RequestBasedWikiUrls(final String base) {
    _base = base;
  }
  
  public String page(final String name) {
    return _base + "/pages/" + Escape.url(name);
  }

  public String root() {
    return _base + "/";
  }

  public String search() {
    return _base + "/pages/FindPage";
  }

  public String feed() {
    return page("RecentChanges") + "/atom.xml";
  }

}
