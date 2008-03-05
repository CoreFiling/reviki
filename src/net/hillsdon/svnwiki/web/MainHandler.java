package net.hillsdon.svnwiki.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.search.ExternalCommitAwareSearchEngine;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.handlers.Attachments;
import net.hillsdon.svnwiki.web.handlers.EditorForPage;
import net.hillsdon.svnwiki.web.handlers.GetAttachment;
import net.hillsdon.svnwiki.web.handlers.GetPage;
import net.hillsdon.svnwiki.web.handlers.History;
import net.hillsdon.svnwiki.web.handlers.SetPage;
import net.hillsdon.svnwiki.web.handlers.UploadAttachment;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.WikiUrls;
import net.hillsdon.svnwiki.wiki.renderer.CreoleMarkupRenderer;

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
  private final RequestHandler _history;
  private final RequestHandler _attachments;
  private final RequestHandler _uploadAttachment;
  private final RequestHandler _getAttachment;

  public MainHandler(final InitialConfiguration configuration) throws IOException {
    ExternalCommitAwareSearchEngine searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory()));
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searchEngine);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    searchEngine.setPageStore(_pageStore);
    MarkupRenderer renderer = new CreoleMarkupRenderer(new PageStoreConfiguration(_pageStore), new InternalLinker(_pageStore));
    _get = new GetPage(_pageStore, searchEngine, renderer);
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_pageStore);
    _history = new History(_pageStore);
    _attachments = new Attachments(_pageStore);
    _uploadAttachment = new UploadAttachment(_pageStore);
    _getAttachment = new GetAttachment(_pageStore);
  }
  
  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
      try {
        WikiUrls urls = new RequestBasedWikiUrls(request);
        boolean isPost = request.getMethod().equals("POST");
        String requestURL = request.getRequestURL().toString();
        if (urls.isPage(requestURL)) {
          if (!isPost) {
            if (request.getParameter("history") != null) {
              _history.handle(request, response);
            }
            else {
              _get.handle(request, response);
            }
          }
          else if (request.getParameter("content") == null) {
            _editor.handle(request, response);
          }
          else {
            _set.handle(request, response);
          }
        }
        else if (urls.isAttachmentsDir(requestURL)) {
          (isPost ? _uploadAttachment : _attachments).handle(request, response);
        }
        else if (urls.isAttachment(requestURL)) {
          _getAttachment.handle(request, response);
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
