package net.hillsdon.reviki.web.dispatching;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.configuration.DeploymentConfiguration;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.JumpToWikiUrl;
import net.hillsdon.reviki.web.handlers.ListWikis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DispatcherImpl implements Dispatcher {
  
  private static final Log LOG = LogFactory.getLog(DispatcherServlet.class);

  private final DeploymentConfiguration _configuration;
  private final ServletContext _servletContext;

  private final ListWikis _list;
  private final WikiChoiceImpl _choice;
  private final JumpToWikiUrl _jumpToWiki;

  public DispatcherImpl(ServletContext servletContext, DeploymentConfiguration configuration, ListWikis list, WikiChoiceImpl choice, JumpToWikiUrl jumpToWiki) {
    _servletContext = servletContext;
    _configuration = configuration;
    _list = list;
    _choice = choice;
    _jumpToWiki = jumpToWiki;
    
    _configuration.load();
  }

  public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    request.setAttribute("cssUrl", request.getContextPath() + "/resources/default-style.css");

    ConsumedPath path = new ConsumedPath(request);
    try {
      View view = handle(path, request, response);
      if (view != null) {
        view.render(request, response);
      }
    }
    catch (NotFoundException ex) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    catch (Exception ex) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      handleException(request, response, ex);
    }
  }

  // This should be moved out of here...
  private View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String initial = path.next();
    if ("root".equals(initial)) {
      // ... an internal hack to enable the dispatcher to handle "/".
      final String defaultWiki = _configuration.getDefaultWiki();
      if (defaultWiki != null) {
        return new RedirectView(_servletContext.getContextPath() + "/pages/" + Escape.url(defaultWiki) + "/FrontPage");
      }
      else {
        return new RedirectView(_servletContext.getContextPath() + "/list");
      }
    }
    else if ("pages".equals(initial)) {
      return _choice.handle(path, request, response);
    }
    else if ("jump".equals(initial)) {
      return _jumpToWiki.handle(path, request, response);
    }
    else if ("list".equals(initial)) {
      return _list.handle(path, request, response);
    }
    throw new NotFoundException();
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
