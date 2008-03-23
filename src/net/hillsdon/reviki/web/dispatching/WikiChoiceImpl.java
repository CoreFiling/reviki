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
package net.hillsdon.reviki.web.dispatching;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.di.ApplicationSession;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;

public class WikiChoiceImpl implements WikiChoice {

  private final Map<WikiConfiguration, RequestHandler> _wikis = new ConcurrentHashMap<WikiConfiguration, RequestHandler>();
  private final DeploymentConfiguration _configuration;
  private final ApplicationSession _applicationSession;

  public WikiChoiceImpl(final DeploymentConfiguration configuration, final ApplicationSession applicationSession) {
    _configuration = configuration;
    _applicationSession = applicationSession;
  }

  public WikiHandler addWiki(final WikiConfiguration configuration) {
    WikiHandler handler = _applicationSession.createWikiSession(configuration).getWikiHandler();
    _wikis.put(configuration, handler);
    return handler;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    WikiConfiguration configuration = getWikiConfiguration(path);
    request.setAttribute("wikiName", configuration.getWikiName());
    request.setAttribute(WikiHandlerImpl.ATTRIBUTE_WIKI_IS_VALID, configuration.isComplete());
    RequestBasedWikiUrls.create(request, configuration);
    RequestHandler handler = getWikiHandler(configuration, path);
    return handler.handle(path, request, response);
  }

  private RequestHandler getWikiHandler(final WikiConfiguration perWikiConfiguration, final ConsumedPath path) throws NotFoundException {
    RequestHandler wiki = _wikis.get(perWikiConfiguration);
    boolean reconfigure = "ConfigSvnLocation".equals(path.peek());
    if (wiki == null || reconfigure) {
      // At the moment we lazily install wiki handlers.  Fix this when adding a wiki list?
      if (perWikiConfiguration.isComplete() && !reconfigure) {
        return addWiki(perWikiConfiguration);
      }
      return new ConfigureWikiHandler(_configuration, this, perWikiConfiguration);
    }
    return wiki;
  }

  private WikiConfiguration getWikiConfiguration(final ConsumedPath path) throws NotFoundException {
    boolean asDefault = false;
    String wikiName = path.peek();
    if (wikiName == null) {
      throw new NotFoundException();
    }
    if (wikiName.matches("^\\p{Lu}.*")) {
      // It's not a subwiki, rather a request for a page in the default wiki.
      asDefault = true;
      wikiName = _configuration.getDefaultWiki();
      if (wikiName == null) {
        throw new NotFoundException();
      }
    }
    else {
      path.next();
    }
    String givenWikiName = asDefault ? null : wikiName;
    return _configuration.getConfiguration(wikiName, givenWikiName);
  }

}
