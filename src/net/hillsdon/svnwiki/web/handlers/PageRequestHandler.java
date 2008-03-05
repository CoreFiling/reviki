package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.RequestHandler;

/**
 * Common super-class to parse the page name out of the URL.
 * 
 * @author mth
 */
public abstract class PageRequestHandler  implements RequestHandler {

  private PageStore _store;

  public PageRequestHandler(final PageStore store) {
    _store = store;
  }
  
  public final void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String uri = request.getRequestURI();
    String page = uri.substring(uri.lastIndexOf('/') + 1);
    handlePage(request, response, _store, page);
  }

  public abstract void handlePage(HttpServletRequest request, HttpServletResponse response, PageStore store, String page) throws Exception;

}
