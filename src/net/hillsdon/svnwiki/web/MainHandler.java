package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.handlers.Attachments;
import net.hillsdon.svnwiki.web.handlers.EditorForPage;
import net.hillsdon.svnwiki.web.handlers.GetAttachment;
import net.hillsdon.svnwiki.web.handlers.GetPage;
import net.hillsdon.svnwiki.web.handlers.History;
import net.hillsdon.svnwiki.web.handlers.Search;
import net.hillsdon.svnwiki.web.handlers.SetPage;
import net.hillsdon.svnwiki.web.handlers.UploadAttachment;
import net.hillsdon.svnwiki.wiki.CreoleMarkupRenderer;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

/**
 * All requests come through here if we're correctly configured.
 * 
 * @author mth
 */
public class MainHandler implements RequestHandler {

  private final RequestScopedThreadLocalPageStore _pageStore;
  private final RequestHandler _get;
  private final RequestHandler _editor;
  private final RequestHandler _set;
  private final RequestHandler _search;
  private final RequestHandler _history;
  private final RequestHandler _attachments;
  private final RequestHandler _uploadAttachment;
  private final RequestHandler _getAttachment;

  public MainHandler(final InitialConfiguration configuration) {
    LuceneSearcher searcher = new LuceneSearcher(configuration.getSearchIndexDirectory());
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searcher);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    //MarkupRenderer renderer = new RadeoxMarkupRenderer(new PageStoreConfiguration(_pageStore), _pageStore);
    MarkupRenderer renderer = new CreoleMarkupRenderer();
    _get = new GetPage(_pageStore, searcher, renderer);
    _search = new Search(_pageStore, searcher);
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_pageStore);
    _history = new History(_pageStore);
    _attachments = new Attachments(_pageStore);
    _uploadAttachment = new UploadAttachment(_pageStore);
    _getAttachment = new GetAttachment(_pageStore);
  }
  
  private boolean isPost(final HttpServletRequest request) {
    return "POST".equals(request.getMethod());
  }
  
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request); 
      try {
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(request.getContextPath().length());
        if (path.startsWith("/pages/")) {
          if (isPost(request)) {
            if (path.contains("/attachments/")) {
              _uploadAttachment.handle(request, response);
            }
            else if (request.getParameter("content") == null) {
              _editor.handle(request, response);
            }
            else {
              _set.handle(request, response);
            }
          }
          else {
            if (path.endsWith("/attachments/")) {
              _attachments.handle(request, response);
            }
            else if (path.contains("/attachments/")) {
              _getAttachment.handle(request, response);
            }
            else if (request.getParameter("history") != null) {
              _history.handle(request, response);
            }
            else {
              _get.handle(request, response);
            }
          }
        }
        else if (path.startsWith("/search")) {
          _search.handle(request, response);
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
