package net.hillsdon.svnwiki.web.dispatching;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.ConfigurationLocation;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestAttributes;
import net.hillsdon.svnwiki.web.common.RequestHandler;

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
  private RequestHandler _handler;


  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    ConfigurationLocation configuration = new ConfigurationLocation();
    configuration.load();
    _handler = new WikiChoice(config.getServletContext(), configuration);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    ConsumedPath path = new ConsumedPath(request);
    try {
      _handler.handle(path, request, response);
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
