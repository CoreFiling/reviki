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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.di.ApplicationSession;
import net.hillsdon.reviki.web.dispatching.Dispatcher;

import org.picocontainer.PicoBuilder;

/**
 * We should probably find a web framework that doesn't suck but this'll do for now.
 * 
 * @author mth
 */
public class DispatcherServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private Dispatcher _dispatcher;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    // This package cycle is fundamental... I figure we'd fix it by
    // putting the impl class name in the web.xml so this is a reasonable
    // temporary step to get back to zero cycles.
    try {
      _dispatcher = new PicoBuilder().build()
        .addComponent(Class.forName("net.hillsdon.reviki.di.impl.ApplicationSessionImpl"))
        .addComponent(config)
        .addComponent(config.getServletContext())
        .getComponent(ApplicationSession.class).getDispatcher();
    }
    catch (ClassNotFoundException e) {
      throw new ServletException("Root session class not found", e);
    }
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    _dispatcher.handle(request, response);
  }

}
