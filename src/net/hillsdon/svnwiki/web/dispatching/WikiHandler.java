package net.hillsdon.svnwiki.web.dispatching;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.configuration.PageStoreConfiguration;
import net.hillsdon.svnwiki.configuration.PerWikiInitialConfiguration;
import net.hillsdon.svnwiki.search.ExternalCommitAwareSearchEngine;
import net.hillsdon.svnwiki.search.LuceneSearcher;
import net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreAuthenticationException;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestHandler;
import net.hillsdon.svnwiki.web.handlers.PageHandler;
import net.hillsdon.svnwiki.web.vcintegration.BasicAuthPassThroughPageStoreFactory;
import net.hillsdon.svnwiki.web.vcintegration.RequestScopedThreadLocalPageStore;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.renderer.SvnWikiRenderer;

/**
 * A particular wiki (sub-wiki, whatever).
 * 
 * @author mth
 */
public class WikiHandler implements RequestHandler {

  private final PerWikiInitialConfiguration _configuration;
  private final RequestScopedThreadLocalPageStore _pageStore;
  private final MarkupRenderer _renderer;
  private final ConfigPageCachingPageStore _cachingPageStore;
  
  private final PageHandler _handler;
  private final InternalLinker _internalLinker;

  public WikiHandler(final PerWikiInitialConfiguration configuration, final String contextPath) {
    _configuration = configuration;
    // The search engine is informed of page changes by a delegating page store.
    // A delegating search engine checks it is up-to-date using the page store
    // so we have a circularity here, but a useful one.
    ExternalCommitAwareSearchEngine searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory()));
    PageStoreFactory factory = new BasicAuthPassThroughPageStoreFactory(configuration.getUrl(), searchEngine);
    _pageStore = new RequestScopedThreadLocalPageStore(factory);
    searchEngine.setPageStore(_pageStore);
    _cachingPageStore = new ConfigPageCachingPageStore(_pageStore);
    _internalLinker = new InternalLinker(contextPath, configuration.getGivenWikiName(), _cachingPageStore);
    _renderer = new SvnWikiRenderer(new PageStoreConfiguration(_pageStore), _internalLinker);
    _handler = new PageHandler(_cachingPageStore, searchEngine, _renderer);
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    request.setAttribute("wikiName", _configuration.getWikiName());
    request.setAttribute("wikiIsValid", _configuration.isComplete());
    request.setAttribute("internalLinker", _internalLinker);
    try {
      // Handle the lifecycle of the thread-local request dependent page store.
      _pageStore.create(request);
      try {
        addSideBarToRequest(request);
        _handler.handle(path, request, response);
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
        // Don't try to show wiki header/footer.
        request.setAttribute("wikiIsValid", false);
        throw ex;
      }
    }
  }

  private void addSideBarToRequest(final HttpServletRequest request) throws PageStoreException, IOException {
    PageReference sidebar = new PageReference("ConfigSideBar");
    StringWriter sidebarHtml = new StringWriter();
    PageInfo configSideBar = _cachingPageStore.get(sidebar, -1);
    _renderer.render(sidebar, configSideBar.getContent(), sidebarHtml);
    request.setAttribute("sidebar", sidebarHtml.toString());
  }

  private void requestAuthentication(final HttpServletResponse response) throws IOException {
    response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
    response.sendError(401);
  }

}
