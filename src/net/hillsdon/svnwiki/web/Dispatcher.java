package net.hillsdon.svnwiki.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.wiki.RadeoxMarkupRenderer;

/**
 * We should probably find a web framework that doesn't suck but this'll do for now.
 * 
 * @author mth
 */
public class Dispatcher extends HttpServlet {
  
  private static final long serialVersionUID = 1L;

  /**
   * What's the best way to configure this?
   */
  private static final String URL = "http://localhost/svn/usr/mth/wiki/";
  
  private RequestHandler _get;
  private RequestHandler _editor;
  private RequestHandler _set;
  private RequestHandler _recentChanges;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      PageStoreFactory psf = new BasicAuthPassThroughPageStoreFactory(URL);
      _recentChanges = new AllPages(psf);
      _get = new GetPage(psf, new RadeoxMarkupRenderer());
      _editor = new EditorForPage(psf);
      _set = new SetPage(psf);
    }
    catch (Exception ex) {
      throw new ServletException("Configuraton problem.", ex);
    }
  }
  
  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    try {
      if ("GET".equals(request.getMethod())) {
        _get.handle(request, response);
      }
      else if ("POST".equals(request.getMethod())) {
        if (request.getParameter("content") == null) {
          _editor.handle(request, response);
        }
        else {
          _set.handle(request, response);
          response.sendRedirect(request.getRequestURI());
        }
      }
    }
    catch (PageStoreAuthenticationException ex) {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
      response.sendError(401);
    }
    catch (Exception ex) {
      throw new ServletException(ex);
    }
  }
  
}
