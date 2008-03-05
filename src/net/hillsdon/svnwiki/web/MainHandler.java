package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.handlers.EditorForPage;
import net.hillsdon.svnwiki.web.handlers.GetPage;
import net.hillsdon.svnwiki.web.handlers.SetPage;
import net.hillsdon.svnwiki.wiki.RadeoxMarkupRenderer;

public class MainHandler implements RequestHandler {

  private RequestScopedThreadLocalPageStore _pageStore;
  private RequestHandler _get;
  private RequestHandler _editor;
  private RequestHandler _set;

  public MainHandler(final Configuration configuration) throws PageStoreException {
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl());
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    _get = new GetPage(_pageStore, new RadeoxMarkupRenderer(_pageStore));
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_pageStore);
  }
  
  @Override
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
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
      finally {
        _pageStore.destroy();
      }
    }
    catch (PageStoreAuthenticationException ex) {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
      response.sendError(401);
    }
  }

}
