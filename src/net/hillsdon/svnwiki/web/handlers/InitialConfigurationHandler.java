package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.web.RequestHandler;

public class InitialConfigurationHandler implements RequestHandler {

  private final InitialConfiguration _configuration;

  public InitialConfigurationHandler(final InitialConfiguration configuration) {
    _configuration = configuration;
  }
  
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if ("GET".equals(request.getMethod())) {
      showView(request, response);
    }
    else if ("POST".equals(request.getMethod())) {
      String url = request.getParameter("url");
      try {
        _configuration.setUrl(url);
        _configuration.save();
        response.sendRedirect(request.getRequestURI());
      }
      catch (IllegalArgumentException ex) {
        request.setAttribute("error", ex.getMessage());
        showView(request, response);
      }
    }
  }

  private void showView(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("configuration", _configuration);
    request.getRequestDispatcher("/WEB-INF/templates/Configuration.jsp").include(request, response);
  }
      
}
