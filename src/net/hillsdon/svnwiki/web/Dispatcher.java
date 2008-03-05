package net.hillsdon.svnwiki.web;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.web.handlers.InitialConfigurationHandler;

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
  private InitialConfiguration _configuration;
  private RequestHandler _currentHandler;
  private RequestHandler _configurationHandler;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    _configuration = new InitialConfiguration();
    _configuration.load();
    _configurationHandler = new InitialConfigurationHandler(_configuration);
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
    response.setContentType("text/html");
    response.setCharacterEncoding("utf-8");
    setCurrentHandler();
    try {
      if ((request.getContextPath() + "/configuration").equals(request.getRequestURI())) {
        _configurationHandler.handle(request, response);
      }
      else {
        _currentHandler.handle(request, response);
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
