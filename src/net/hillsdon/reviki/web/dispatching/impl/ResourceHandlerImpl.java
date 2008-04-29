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

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.dispatching.ResourceHandler;

public class ResourceHandlerImpl implements ResourceHandler {

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String resource = path.next();
    if (resource == null || path.hasNext()) {
      throw new NotFoundException();
    }
    return new View() {
      public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        request.getRequestDispatcher("/resources/" + Escape.url(resource)).forward(request, response);
      }
    };
  }

}
