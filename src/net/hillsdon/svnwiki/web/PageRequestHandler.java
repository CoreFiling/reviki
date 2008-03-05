package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public abstract class PageRequestHandler  implements RequestHandler {

  private PageStoreFactory _pageStoreFactory;

  public PageRequestHandler(final PageStoreFactory pageStoreFactory) {
    _pageStoreFactory = pageStoreFactory;
  }
  
  @Override
  public final void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    PageStore store = _pageStoreFactory.newInstance(request);
    String uri = request.getRequestURI();
    String page = uri.substring(uri.lastIndexOf('/') + 1);
    handlePage(request, response, store, page);
  }

  public abstract void handlePage(HttpServletRequest request, HttpServletResponse response, PageStore store, String page) throws Exception;

}
