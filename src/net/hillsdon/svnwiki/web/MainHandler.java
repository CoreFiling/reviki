package net.hillsdon.svnwiki.web;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.InitialConfiguration;
import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.search.ExternalCommitAwareSearchEngine;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
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
  private MarkupRenderer _renderer;
  private ConfigPageCachingPageStore _cachingPageStore;

  public MainHandler(final InitialConfiguration configuration) throws IOException {
    // The search engine is informed of page changes by a delegating page store.
    // A delegating search engine checks it is up-to-date using the page store
    // so we have a circularity here, but a useful one.
    ExternalCommitAwareSearchEngine searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory()));
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searchEngine);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    searchEngine.setPageStore(_pageStore);
    _cachingPageStore = new ConfigPageCachingPageStore(_pageStore);
    
    _renderer = new CreoleMarkupRenderer(new PageStoreConfiguration(_cachingPageStore), new InternalLinker(_cachingPageStore));
    _get = new GetPage(_cachingPageStore, searchEngine, _renderer);
    // It is important to use the non-caching page store here.  It is ok to view 
    // something out of date but users must edit the latest revision or else they
    // won't be able to commit.
    _editor = new EditorForPage(_pageStore);
    _set = new SetPage(_cachingPageStore);
    _history = new History(_cachingPageStore);
    _attachments = new Attachments(_cachingPageStore);
    _uploadAttachment = new UploadAttachment(_cachingPageStore);
    _getAttachment = new GetAttachment(_cachingPageStore);
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
      try {
        WikiUrls urls = new RequestBasedWikiUrls(request);
        boolean isPost = request.getMethod().equals("POST");
        String requestURL = request.getRequestURL().toString();
        
        PageReference sidebar = new PageReference("ConfigSideBar");
        StringWriter sidebarHtml = new StringWriter();
        PageInfo configSideBar = _cachingPageStore.get(sidebar, -1);
        _renderer.render(sidebar, configSideBar.getContent(), sidebarHtml);
        request.setAttribute("sidebar", sidebarHtml.toString());
        
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
      requestAuthentication(response);
    }
    catch (Exception ex) {
      // Rather horrible, needed at the moment for auth failures during rendering (linking).
      if (ex.getCause() instanceof PageStoreAuthenticationException) {
        requestAuthentication(response);
      }
      else {
        throw ex;
      }
    }
  }

  private void requestAuthentication(final HttpServletResponse response) throws IOException {
    response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
    response.sendError(401);
  }

}
