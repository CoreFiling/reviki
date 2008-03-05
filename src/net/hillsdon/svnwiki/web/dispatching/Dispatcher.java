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
package net.hillsdon.svnwiki.web.dispatching;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.DeploymentConfiguration;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestAttributes;
import net.hillsdon.svnwiki.web.handlers.ListWikis;

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
  private ListWikis _list;


  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    DeploymentConfiguration configuration = new DeploymentConfiguration();
    configuration.load();
    _list = new ListWikis(configuration);
    _choice = new WikiChoice(config.getServletContext(), configuration);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    ConsumedPath path = new ConsumedPath(request);
    try {
      String initial = path.next();
      if ("pages".equals(initial)) {
        _choice.handle(path, request, response);
      }
      else {
        _list.handle(path, request, response);
      }
    }
    catch (NotFoundException ex) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception ex) {
      handleException(request, response, ex);
    }
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
