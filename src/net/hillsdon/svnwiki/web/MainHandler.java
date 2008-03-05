package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.handlers.EditorForPage;
import net.hillsdon.svnwiki.web.handlers.GetPage;
import net.hillsdon.svnwiki.web.handlers.History;
import net.hillsdon.svnwiki.web.handlers.Search;
import net.hillsdon.svnwiki.web.handlers.SetPage;
import net.hillsdon.svnwiki.wiki.RadeoxMarkupRenderer;

/**
 * All requests come through here if we're correctly configured.
 * 
 * @author mth
 */
public class MainHandler implements RequestHandler {

  private RequestScopedThreadLocalPageStore _pageStore;
  private RequestHandler _get;
  private RequestHandler _editor;
  private RequestHandler _set;
  private RequestHandler _search;
  private RequestHandler _history;

  public MainHandler(final InitialConfiguration configuration) {
    LuceneSearcher searcher = new LuceneSearcher(configuration.getSearchIndexDirectory());
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searcher);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    _get = new GetPage(_pageStore, searcher, new RadeoxMarkupRenderer(new PageStoreConfiguration(_pageStore), _pageStore));
    _search = new Search(_pageStore, searcher);
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_pageStore);
    _history = new History(_pageStore);
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
            if (request.getParameter("history") != null) {
              _history.handle(request, response);
            }
            else {
              _get.handle(request, response);
            }
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
