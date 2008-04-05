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

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.wiki.WikiUrls;

/**
 * Icky.
 * 
 * We should not base the URLs on the request.
 * 
 * We need to know the public base URL anyway really* for e.g. atom ids
 * (we may be proxied through e.g. Apache).
 * 
 * @author mth
 */
public class RequestBasedWikiUrls implements WikiUrls {

  public static void create(final HttpServletRequest request, final WikiConfiguration configuration) {
    request.setAttribute(RequestBasedWikiUrls.class.getName(), new RequestBasedWikiUrls(request, configuration));
  }
  
  public static WikiUrls get(final HttpServletRequest request) {
    return (WikiUrls) request.getAttribute(RequestBasedWikiUrls.class.getName());
  }
  
  private String _base;
  private final WikiConfiguration _configuration;

  public RequestBasedWikiUrls(final HttpServletRequest request, final WikiConfiguration configuration) {
    _configuration = configuration;
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    _base = requestURL.substring(0, requestURL.length() - path.length());
  }
  
  public String root() {
    String givenWikiName = _configuration.getGivenWikiName();
    String result = _base + "/pages/";
    if (givenWikiName != null) {
      result += Escape.url(givenWikiName) + "/";
    }
    return result;
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
    return _base + "/resources/favicon.ico";
  }

}
