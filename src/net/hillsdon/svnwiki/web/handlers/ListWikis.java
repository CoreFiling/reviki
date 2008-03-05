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
package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.DeploymentConfiguration;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestHandler;

public class ListWikis implements RequestHandler {

  private final DeploymentConfiguration _configuration;

  public ListWikis(final DeploymentConfiguration configuration) {
    _configuration = configuration;
  }
  
  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException, ServletException {
    request.setAttribute("configuration", _configuration);
    request.getRequestDispatcher("/WEB-INF/templates/ListWikis.jsp").include(request, response);
  }

}
