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

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.configuration.PropertiesDeploymentConfiguration;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.impl.JumpToWikiUrlImpl;
import net.hillsdon.reviki.web.handlers.impl.ListWikisImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We should probably find a web framework that doesn't suck but this'll do for now.
 * 
 * @author mth
 */
public class Dispatcher extends HttpServlet {
  
  private static final Log LOG = LogFactory.getLog(Dispatcher.class);
  
  private static final long serialVersionUID = 1L;

  private WikiChoice _choice;
  private ListWikisImpl _list;
  private JumpToWikiUrlImpl _jump;


  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    DeploymentConfiguration configuration = new PropertiesDeploymentConfiguration();
    configuration.load();
    _list = new ListWikisImpl(configuration);
    _jump = new JumpToWikiUrlImpl();
    _choice = new WikiChoice(config.getServletContext(), configuration);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    request.setAttribute("cssUrl", request.getContextPath() + "/resources/default-style.css");

    ConsumedPath path = new ConsumedPath(request);
    try {
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
  }

  private View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String initial = path.next();
    if ("pages".equals(initial)) {
      return _choice.handle(path, request, response);
    }
    else if ("jump".equals(initial)) {
      return _jump.handle(path, request, response);
    }
    return _list.handle(path, request, response);
  }

  private void handleException(final HttpServletRequest request, final HttpServletResponse response, Exception ex) throws ServletException, IOException {
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
