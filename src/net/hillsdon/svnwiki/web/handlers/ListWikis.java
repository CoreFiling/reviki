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
