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
package net.hillsdon.reviki.web.dispatching.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.ActiveWikis;
import net.hillsdon.reviki.web.redirect.RedirectToPageView;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.impl.WikiUrlsImpl;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;

public class ConfigureWikiHandler implements RequestHandler {
  
  private final WikiConfiguration _perWikiConfiguration;
  private final DeploymentConfiguration _configuration;
  private final ActiveWikis _activeWikis;
  private final ApplicationUrls _applicationUrls;

  public ConfigureWikiHandler(final DeploymentConfiguration configuration, final ActiveWikis activeWikis, final WikiConfiguration perWikiConfiguration, final ApplicationUrls applicationUrls) {
    _configuration = configuration;
    _activeWikis = activeWikis;
    _perWikiConfiguration = perWikiConfiguration;
    _applicationUrls = applicationUrls;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if (!_configuration.isEditable()) {
      throw new InvalidInputException("Editing of the configuration is currently disabled.");
    }
    request.setAttribute("configuration", _perWikiConfiguration);
    if ("POST".equals(request.getMethod())) {
      String url = request.getParameter("url");
      try {
        _perWikiConfiguration.setUrl(url);
        _perWikiConfiguration.save();
        if (_perWikiConfiguration.isComplete()) {
          _activeWikis.addWiki(_perWikiConfiguration);
        }
        return new RedirectToPageView(new WikiUrlsImpl(_applicationUrls, _perWikiConfiguration), BuiltInPageReferences.PAGE_FRONT_PAGE);
      }
      catch (IllegalArgumentException ex) {
        request.setAttribute("error", ex.getMessage());
        return new JspView("Configuration");
      }
    }
    else {
      return new JspView("Configuration");
    }
  }
  
}