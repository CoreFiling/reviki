package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.web.Configuration;
import net.hillsdon.svnwiki.web.RequestHandler;

public class ConfigurationHandler implements RequestHandler {

  private final Configuration _configuration;

  public ConfigurationHandler(final Configuration configuration) {
    _configuration = configuration;
  }
  
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if ("GET".equals(request.getMethod())) {
      request.setAttribute("configuration", _configuration);
      request.getRequestDispatcher("/WEB-INF/templates/Configuration.jsp").forward(request, response);
    }
    else if ("POST".equals(request.getMethod())){
      String url = request.getParameter("url");
      _configuration.setUrl(url);
      response.sendRedirect(request.getRequestURI());
    }
  }
      
}
