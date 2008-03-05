package net.hillsdon.svnwiki.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.web.handlers.ConfigurationHandler;

/**
 * We should probably find a web framework that doesn't suck but this'll do for now.
 * 
 * @author mth
 */
public class Dispatcher extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  private Configuration _configuration;
  private RequestHandler _currentHandler;
  private RequestHandler _configurationHandler;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    _configuration = new Configuration();
    _configuration.load();
    _configurationHandler = new ConfigurationHandler(_configuration);
  }

  private void setCurrentHandler() {
    try {
      if (_configurationHandler == _configurationHandler && _configuration.isComplete()) {
        _currentHandler = new MainHandler(_configuration);
      }
      else {
        _currentHandler = _configurationHandler;
      }
    }
    catch (Exception ex) {
      _currentHandler = _configurationHandler;
    }
  }
  
  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    setCurrentHandler();
    try {
      if ((request.getContextPath() + "/configuration").equals(request.getRequestURI())) {
        _configurationHandler.handle(request, response);
      }
      else {
        _currentHandler.handle(request, response);
      }
    }
    catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
  
}
