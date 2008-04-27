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

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;


public class ApplicationUrlsImpl implements ApplicationUrls {

  private static String getBaseUrl(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String base = requestURL.substring(0, requestURL.length() - path.length());
    return base;
  }

  private final String _base;

  public ApplicationUrlsImpl(final HttpServletRequest request) {
    this(getBaseUrl(request));
  }
  
  public ApplicationUrlsImpl(final String base) {
    _base = base;
  }

  public WikiUrls get(final String name) {
    return new WikiUrlsImpl(this, name);
  }

  public String list() {
    return url("/list");
  }

  public String url(String relative) {
    return _base + relative;
  }

}
