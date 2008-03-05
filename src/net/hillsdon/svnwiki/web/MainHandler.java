package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.handlers.EditorForPage;
import net.hillsdon.svnwiki.web.handlers.GetPage;
import net.hillsdon.svnwiki.web.handlers.Search;
import net.hillsdon.svnwiki.web.handlers.SetPage;
import net.hillsdon.svnwiki.wiki.RadeoxMarkupRenderer;

public class MainHandler implements RequestHandler {

  private RequestScopedThreadLocalPageStore _pageStore;
  private RequestHandler _get;
  private RequestHandler _editor;
  private RequestHandler _set;
  private RequestHandler _search;

  public MainHandler(final Configuration configuration) {
    LuceneSearcher searcher = new LuceneSearcher(configuration.getSearchIndexDirectory());
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searcher);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    _search = new Search(searcher);
    _get = new GetPage(_pageStore, new RadeoxMarkupRenderer(_pageStore));
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_pageStore);
  }
  
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
      try {
        if ("GET".equals(request.getMethod())) {
          if ((request.getContextPath() + "/search").equals(request.getRequestURI())) {
            _search.handle(request, response);
          }
          else {
            _get.handle(request, response);
          }
        }
        else if ("POST".equals(request.getMethod())) {
          if (request.getParameter("content") == null) {
            _editor.handle(request, response);
          }
          else {
            _set.handle(request, response);
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
