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
        request.getRequestDispatcher("/resources/" + Escape.url(resource)).include(request, response);
      }
    };
  }

}
