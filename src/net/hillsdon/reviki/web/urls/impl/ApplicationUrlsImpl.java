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

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;

/**
 * Determines base URL from the request.
 * 
 * @author mth
 */
public class ApplicationUrlsImpl implements ApplicationUrls {

  private static String getBaseUrl(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String base = requestURL.substring(0, requestURL.length() - path.length());
    return base;
  }

  private final String _base;
  private final DeploymentConfiguration _deploymentConfiguration;

  public ApplicationUrlsImpl(final HttpServletRequest request, final DeploymentConfiguration deploymentConfiguration) {
    this(getBaseUrl(request), deploymentConfiguration);
  }
  
  public ApplicationUrlsImpl(final String base, final DeploymentConfiguration deploymentConfiguration) {
    _base = base;
    _deploymentConfiguration = deploymentConfiguration;
  }

  public WikiUrls get(final String name) {
    return get(name, name);
  }

  public String list() {
    return url("/list");
  }

  public String url(final String relative) {
    return _base + relative;
  }

  public WikiUrls get(String name, String givenWikiName) {
    return new WikiUrlsImpl(this, _deploymentConfiguration.getConfiguration(name));
  }

  public String resource(String path) {
    return url("/resources/" + path);
  }

}
