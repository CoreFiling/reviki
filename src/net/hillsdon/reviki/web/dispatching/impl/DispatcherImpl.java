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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.Dispatcher;
import net.hillsdon.reviki.web.handlers.JumpToWikiUrl;
import net.hillsdon.reviki.web.handlers.ListWikis;
import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.vcintegration.RequestCompletedHandler;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static java.lang.String.format;

public class DispatcherImpl implements Dispatcher {
  
  private static final Log LOG = LogFactory.getLog(DispatcherServlet.class);

  private final DeploymentConfiguration _configuration;

  private final ListWikis _list;
  private final WikiChoiceImpl _choice;
  private final JumpToWikiUrl _jumpToWiki;
  private final ApplicationUrls _applicationUrls;
  private final RequestLifecycleAwareManager _requestLifecycleAwareManager;
  private final RequestCompletedHandler _requestCompletedHandler;

  public DispatcherImpl(final DeploymentConfiguration configuration, final ListWikis list, final WikiChoiceImpl choice, final JumpToWikiUrl jumpToWiki, final ApplicationUrls applicationUrls, final RequestLifecycleAwareManager requestLifecycleAwareManager, final RequestCompletedHandler requestCompletedHandler) {
    _configuration = configuration;
    _list = list;
    _choice = choice;
    _jumpToWiki = jumpToWiki;
    _applicationUrls = applicationUrls;
    _requestLifecycleAwareManager = requestLifecycleAwareManager;
    _requestCompletedHandler = requestCompletedHandler;
    
    _configuration.load();
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    request.setAttribute(ApplicationUrls.KEY, _applicationUrls);
    ConsumedPath path = new ConsumedPath(request);
    try {
      _requestLifecycleAwareManager.requestStarted(request);
      View view = handle(path, request, response);
      if (view != null) {
        view.render(request, response);
      }
    }
    catch (NotFoundException ex) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception ex) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      handleException(request, response, ex);
    }
    finally {
      _requestCompletedHandler.requestComplete();
    }
  }

  private View handle(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    // Skip the 'dispatcher' path we're bound to and process the rest.
    path = path.consume();
    
    // These special URLs don't really fit...
    if ("jump".equals(path.peek())) {
      return _jumpToWiki.handle(path.consume(), request, response);
    }
    else if ("list".equals(path.peek())) {
      return _list.handle(path.consume(), request, response);
    }
    return _choice.handle(path, request, response);
  }

  private void handleException(final HttpServletRequest request, final HttpServletResponse response, final Exception ex) throws ServletException, IOException {
    String user = (String) request.getAttribute(RequestAttributes.USERNAME);
    if (user == null) {
      user = "[none]";
    }
    String queryString = request.getQueryString();
    String uri = request.getRequestURI() + (queryString == null ? "" : "?" + queryString);
    LOG.error(format("Forwarding to error page for user '%s' accessing '%s'.", user, uri), ex);
    request.setAttribute("exception", ex);
    request.getRequestDispatcher("/WEB-INF/templates/Error.jsp").forward(request, response);
  }
  
}
