/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.web.dispatching;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.fij.core.Factory;
import net.hillsdon.reviki.configuration.PageStoreConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.search.ExternalCommitAwareSearchEngine;
import net.hillsdon.reviki.search.LuceneSearcher;
import net.hillsdon.reviki.vc.ChangeNotificationDispatcherImpl;
import net.hillsdon.reviki.vc.ChangeNotificationDospatcher;
import net.hillsdon.reviki.vc.ConfigPageCachingPageStore;
import net.hillsdon.reviki.vc.DeletedRevisionTracker;
import net.hillsdon.reviki.vc.InMemoryDeletedRevisionTracker;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RequestHandler;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.Attachments;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.handlers.RegularPage;
import net.hillsdon.reviki.web.handlers.SpecialPages;
import net.hillsdon.reviki.web.handlers.impl.AllPagesImpl;
import net.hillsdon.reviki.web.handlers.impl.AttachmentsImpl;
import net.hillsdon.reviki.web.handlers.impl.FindPageImpl;
import net.hillsdon.reviki.web.handlers.impl.OrphanedPagesImpl;
import net.hillsdon.reviki.web.handlers.impl.PageHandlerImpl;
import net.hillsdon.reviki.web.handlers.impl.RecentChangesImpl;
import net.hillsdon.reviki.web.handlers.impl.RegularPageImpl;
import net.hillsdon.reviki.web.handlers.impl.SpecialPagesImpl;
import net.hillsdon.reviki.web.vcintegration.BasicAuthPassThroughBasicSVNOperationsFactory;
import net.hillsdon.reviki.web.vcintegration.PerRequestPageStoreFactory;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalBasicSVNOperations;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalPageStore;
import net.hillsdon.reviki.web.vcintegration.SpecialPagePopulatingPageStore;
import net.hillsdon.reviki.wiki.InternalLinker;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.RenderedPageFactory;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.graph.WikiGraphImpl;
import net.hillsdon.reviki.wiki.macros.IncomingLinksMacro;
import net.hillsdon.reviki.wiki.macros.OutgoingLinksMacro;
import net.hillsdon.reviki.wiki.macros.SearchMacro;
import net.hillsdon.reviki.wiki.plugin.Plugins;
import net.hillsdon.reviki.wiki.plugin.PluginsImpl;
import net.hillsdon.reviki.wiki.renderer.SvnWikiRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import org.picocontainer.PicoBuilder;

/**
 * A particular wiki (sub-wiki, whatever).
 * 
 * @author mth
 */
public class WikiHandler implements RequestHandler {

  private static final class RequestAuthenticationView implements View {
    public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
      response.sendError(401);
    }
  }

  public static final String ATTRIBUTE_WIKI_IS_VALID = "wikiIsValid";
  
  private final RequestScopedThreadLocalPageStore _pageStore;
  private final SvnWikiRenderer _renderer;
  private final ConfigPageCachingPageStore _cachingPageStore;
  private final PageHandler _handler;
  private final InternalLinker _internalLinker;
  private final ExternalCommitAwareSearchEngine _searchEngine;
  private final Plugins _plugins;
  private final ChangeNotificationDospatcher _syncUpdater;
  private final RequestScopedThreadLocalBasicSVNOperations _operations;

  public WikiHandler(final WikiConfiguration configuration, final String contextPath) {
    // Some of this is a bit circular...
    RenderedPageFactory renderedPageFactory = new RenderedPageFactory(new MarkupRenderer() {
      public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
        _renderer.render(page, in, out);
      }
    });
    _searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory(), renderedPageFactory));
    _operations = new RequestScopedThreadLocalBasicSVNOperations(new BasicAuthPassThroughBasicSVNOperationsFactory(configuration.getUrl()));
    DeletedRevisionTracker tracker = new InMemoryDeletedRevisionTracker();
    Factory<PageStore> pageStoreFactory = new PerRequestPageStoreFactory(_searchEngine, tracker, _operations);
    _pageStore = new RequestScopedThreadLocalPageStore(pageStoreFactory);
    _plugins = new PluginsImpl(_pageStore);
    _searchEngine.setPageStore(_pageStore);
    _cachingPageStore = new ConfigPageCachingPageStore(_pageStore);
    _internalLinker = new InternalLinker(contextPath, configuration.getGivenWikiName(), _cachingPageStore);

    final WikiGraph wikiGraph = new WikiGraphImpl(_cachingPageStore, _searchEngine);
    _renderer = new SvnWikiRenderer(new PageStoreConfiguration(_pageStore), _internalLinker, new Accessor<List<Macro>>() {
      public List<Macro> get() {
        List<Macro> macros = new ArrayList<Macro>(Arrays.<Macro>asList(new IncomingLinksMacro(wikiGraph), new OutgoingLinksMacro(wikiGraph), new SearchMacro(_searchEngine)));
        macros.addAll(_plugins.getImplementations(Macro.class));
        return macros;
      }
    });
    
    _handler = new PicoBuilder().build()
      // These should move to being created by the container too.
      .addComponent(_cachingPageStore)
      .addComponent(wikiGraph)
      .addComponent(_internalLinker)
      .addComponent(renderedPageFactory)
      .addComponent(_renderer)
      .addComponent(_searchEngine)
      
      // Handlers.
      .addComponent(PageHandler.class, PageHandlerImpl.class)
      .addComponent(RegularPage.class, RegularPageImpl.class)
      .addComponent(FindPageImpl.class)
      .addComponent(OrphanedPagesImpl.class)
      .addComponent(AllPagesImpl.class)
      .addComponent(RecentChangesImpl.class)
      .addComponent(Attachments.class, AttachmentsImpl.class)
      .addComponent(SpecialPages.class, SpecialPagesImpl.class)
      .getComponent(PageHandler.class);
    
    // Allow plugin classes to depend on the core wiki API.
    _plugins.addPluginAccessibleComponent(_pageStore);
    _plugins.addPluginAccessibleComponent(wikiGraph);
    _plugins.addPluginAccessibleComponent(_searchEngine);
    
    try {
      _syncUpdater = new ChangeNotificationDispatcherImpl(_operations, tracker, _searchEngine, _plugins);
    }
    catch (IOException ex) {
      throw new RuntimeException("Failed to read data required for start-up.", ex);
    }
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    request.setAttribute("cssUrl", _internalLinker.url("ConfigCss") + "?raw");
    request.setAttribute("internalLinker", _internalLinker);
    try {
      // Handle the lifecycle of the thread-local stuff.
      _operations.create(request);
      _pageStore.create(request);
      try {
        _syncUpdater.sync();
        addSideBarEtcToRequest(request);
        // We need to complete the rendering here, so the view can call back into the page store.
        _handler.handle(path, request, response).render(request, response);
        return View.NULL;
      }
      finally {
        _operations.destroy();
        _pageStore.destroy();
      }
    }
    catch (PageStoreAuthenticationException ex) {
      return new RequestAuthenticationView();
    }
    catch (Exception ex) {
      // Rather horrible, needed at the moment for auth failures during rendering (linking).
      if (ex.getCause() instanceof PageStoreAuthenticationException) {
        return new RequestAuthenticationView();
      }
      else {
        // Don't try to show wiki header/footer.
        request.setAttribute(ATTRIBUTE_WIKI_IS_VALID, false);
        throw ex;
      }
    }
  }

  private void addSideBarEtcToRequest(final HttpServletRequest request) throws PageStoreException, IOException {
    for (PageReference ref : SpecialPagePopulatingPageStore.COMPLIMENTARY_CONTENT_PAGES) {
      final String requestVarName = "rendered" + ref.getPath().substring("Config".length());
      StringWriter html = new StringWriter();
      PageInfo page = _cachingPageStore.get(ref, -1);
      _renderer.render(ref, page.getContent(), html);
      request.setAttribute(requestVarName, html.toString());
    }
  }

}
