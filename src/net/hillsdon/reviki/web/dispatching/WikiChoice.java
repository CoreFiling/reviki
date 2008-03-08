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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;

public class WikiChoice implements RequestHandler {

  private final Map<PerWikiInitialConfiguration, RequestHandler> _wikis = new ConcurrentHashMap<PerWikiInitialConfiguration, RequestHandler>();
  private final DeploymentConfiguration _configuration;
  private final ServletContext _servletContext;

  public class ConfigureWikiHandler implements RequestHandler {
    
    private final PerWikiInitialConfiguration _perWikiConfiguration;

    public ConfigureWikiHandler(final PerWikiInitialConfiguration perWikiConfiguration) {
      _perWikiConfiguration = perWikiConfiguration;
    }

    public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
      if (!_configuration.isEditable()) {
        throw new NotFoundException();
      }
      request.setAttribute("configuration", _perWikiConfiguration);
      if ("POST".equals(request.getMethod())) {
        String url = request.getParameter("url");
        try {
          _perWikiConfiguration.setUrl(url);
          _perWikiConfiguration.save();
          if (_perWikiConfiguration.isComplete()) {
            addWiki(_perWikiConfiguration);
          }
          return new RedirectView(request.getRequestURI());
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
  
  public WikiChoice(ServletContext servletContext, final DeploymentConfiguration configuration) {
    _servletContext = servletContext;
    _configuration = configuration;
  }

  private WikiHandler addWiki(final PerWikiInitialConfiguration configuration) {
    WikiHandler handler = new WikiHandler(configuration, _servletContext.getContextPath());
    _wikis.put(configuration, handler);
    return handler;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    PerWikiInitialConfiguration configuration = getWikiConfiguration(path);
    request.setAttribute("wikiName", configuration.getWikiName());
    request.setAttribute(WikiHandler.ATTRIBUTE_WIKI_IS_VALID, configuration.isComplete());
    RequestBasedWikiUrls.create(request, configuration);
    RequestHandler handler = getWikiHandler(configuration, path);
    return handler.handle(path, request, response);
  }

  private RequestHandler getWikiHandler(final PerWikiInitialConfiguration configuration, final ConsumedPath path) throws NotFoundException {
    RequestHandler wiki = _wikis.get(configuration);
    boolean reconfigure = "ConfigSvnLocation".equals(path.peek());
    if (wiki == null || reconfigure) {
      // At the moment we lazily install wiki handlers.  Fix this when adding a wiki list?
      if (configuration.isComplete() && !reconfigure) {
        return addWiki(configuration);
      }
      return new ConfigureWikiHandler(configuration);
    }
    return wiki;
  }

  private PerWikiInitialConfiguration getWikiConfiguration(final ConsumedPath path) throws NotFoundException {
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
    
    return new PerWikiInitialConfiguration(_configuration, asDefault ? null : wikiName, wikiName);
  }

}
