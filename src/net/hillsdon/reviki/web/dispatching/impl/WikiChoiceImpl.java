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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.di.ApplicationSession;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.WikiChoice;
import net.hillsdon.reviki.web.dispatching.WikiHandler;
import net.hillsdon.reviki.web.redirect.RedirectToPageView;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;

public class WikiChoiceImpl implements WikiChoice {

  private final Map<WikiConfiguration, RequestHandler> _wikis = new ConcurrentHashMap<WikiConfiguration, RequestHandler>();
  private final DeploymentConfiguration _configuration;
  private final ApplicationSession _applicationSession;
  private final ApplicationUrls _applicationUrls;

  public WikiChoiceImpl(final DeploymentConfiguration configuration, final ApplicationSession applicationSession, final ApplicationUrls applicationUrls) {
    _configuration = configuration;
    _applicationSession = applicationSession;
    _applicationUrls = applicationUrls;

    _configuration.load();
    for (String wikiName : _configuration.getWikiNames()) {
      // These wiki configurations could be broken but we can't really report the
      // error now so just wait for the user to find them broken.
      WikiConfiguration perWikiConfiguration = _configuration.getConfiguration(wikiName);
      installHandler(perWikiConfiguration, createWikiHandler(perWikiConfiguration));
    }
  }

  public WikiHandler createWikiHandler(WikiConfiguration configuration) {
    return _applicationSession.createWikiSession(configuration).getWikiHandler();
  }
  
  public void installHandler(WikiConfiguration configuration, WikiHandler handler) {
    _wikis.put(configuration, handler);
  }
  
  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    WikiConfiguration configuration = getWikiConfiguration(path);
    request.setAttribute("wikiName", configuration.getWikiName());
    RequestHandler handler = getWikiHandler(request, configuration, path);
    return handler.handle(path, request, response);
  }

  private RequestHandler getWikiHandler(final HttpServletRequest request, final WikiConfiguration perWikiConfiguration, final ConsumedPath path) throws NotFoundException {
    final RequestHandler wiki = _wikis.get(perWikiConfiguration);
    if ("ConfigSvnLocation".equals(path.peek())) {
      return new ConfigureWikiHandler(_configuration, this, perWikiConfiguration, _applicationUrls);
    }
    else if (wiki == null) {
      return new RequestHandler() {
        public View handle(ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
          return RedirectToPageView.create(BuiltInPageReferences.CONFIG_SVN, _applicationUrls, perWikiConfiguration);
        }
      };
    }
    request.setAttribute(WikiHandlerImpl.ATTRIBUTE_WIKI_IS_VALID, true);
    return wiki;
  }

  private WikiConfiguration getWikiConfiguration(final ConsumedPath path) throws NotFoundException {
    String wikiName = path.next();
    if (wikiName == null) {
      throw new NotFoundException();
    }
    return _configuration.getConfiguration(wikiName);
  }

}
