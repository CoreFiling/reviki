package net.hillsdon.reviki.di.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.fij.core.Factory;
import net.hillsdon.reviki.configuration.PageStoreConfiguration;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.di.WikiSession;
import net.hillsdon.reviki.search.impl.ExternalCommitAwareSearchEngine;
import net.hillsdon.reviki.search.impl.LuceneSearcher;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.ChangeNotificationDispatcherImpl;
import net.hillsdon.reviki.vc.impl.ConfigPageCachingPageStore;
import net.hillsdon.reviki.vc.impl.DeletedRevisionTracker;
import net.hillsdon.reviki.vc.impl.InMemoryDeletedRevisionTracker;
import net.hillsdon.reviki.web.dispatching.WikiHandler;
import net.hillsdon.reviki.web.dispatching.impl.WikiHandlerImpl;
import net.hillsdon.reviki.web.handlers.Attachments;
import net.hillsdon.reviki.web.handlers.EditorForPage;
import net.hillsdon.reviki.web.handlers.GetAttachment;
import net.hillsdon.reviki.web.handlers.GetRegularPage;
import net.hillsdon.reviki.web.handlers.History;
import net.hillsdon.reviki.web.handlers.ListAttachments;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.handlers.RegularPage;
import net.hillsdon.reviki.web.handlers.SetPage;
import net.hillsdon.reviki.web.handlers.SpecialPages;
import net.hillsdon.reviki.web.handlers.UploadAttachment;
import net.hillsdon.reviki.web.handlers.impl.AllPagesImpl;
import net.hillsdon.reviki.web.handlers.impl.AttachmentsImpl;
import net.hillsdon.reviki.web.handlers.impl.EditorForPageImpl;
import net.hillsdon.reviki.web.handlers.impl.FindPageImpl;
import net.hillsdon.reviki.web.handlers.impl.GetAttachmentImpl;
import net.hillsdon.reviki.web.handlers.impl.GetRegularPageImpl;
import net.hillsdon.reviki.web.handlers.impl.HistoryImpl;
import net.hillsdon.reviki.web.handlers.impl.ListAttachmentsImpl;
import net.hillsdon.reviki.web.handlers.impl.OrphanedPagesImpl;
import net.hillsdon.reviki.web.handlers.impl.PageHandlerImpl;
import net.hillsdon.reviki.web.handlers.impl.RecentChangesImpl;
import net.hillsdon.reviki.web.handlers.impl.RegularPageImpl;
import net.hillsdon.reviki.web.handlers.impl.SetPageImpl;
import net.hillsdon.reviki.web.handlers.impl.SpecialPagesImpl;
import net.hillsdon.reviki.web.handlers.impl.UploadAttachmentImpl;
import net.hillsdon.reviki.web.vcintegration.BasicAuthPassThroughBasicSVNOperationsFactory;
import net.hillsdon.reviki.web.vcintegration.PerRequestPageStoreFactory;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalBasicSVNOperations;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalPageStore;
import net.hillsdon.reviki.wiki.InternalLinker;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.RenderedPageFactory;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.graph.WikiGraphImpl;
import net.hillsdon.reviki.wiki.macros.IncomingLinksMacro;
import net.hillsdon.reviki.wiki.macros.OutgoingLinksMacro;
import net.hillsdon.reviki.wiki.macros.SearchMacro;
import net.hillsdon.reviki.wiki.plugin.PluginsImpl;
import net.hillsdon.reviki.wiki.renderer.SvnWikiRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import org.picocontainer.MutablePicoContainer;

public class WikiSessionImpl extends AbstractSession implements WikiSession {

  private SvnWikiRenderer _renderer;
  private PluginsImpl _plugins;

  private ExternalCommitAwareSearchEngine _searchEngine;

  public WikiSessionImpl(final ApplicationSessionImpl parent, final WikiConfiguration configuration) {
    super(parent, configuration);
  }

  public WikiHandler getWikiHandler() {
    return getContainer().getComponent(WikiHandlerImpl.class);
  }

  public void configure(final MutablePicoContainer container) {
    // This is cheating!
    // Some of this is a bit circular.  It needs fixing before we can use the di container.
    final WikiConfiguration configuration = container.getComponent(WikiConfiguration.class);
    final ServletContext servletContext = getParentContainer().getComponent(ServletContext.class);
    
    RenderedPageFactory renderedPageFactory = new RenderedPageFactory(new MarkupRenderer() {
      public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
        _renderer.render(page, in, out);
      }
    });
    _searchEngine = new ExternalCommitAwareSearchEngine(new LuceneSearcher(configuration.getSearchIndexDirectory(), renderedPageFactory));
    RequestScopedThreadLocalBasicSVNOperations operations = new RequestScopedThreadLocalBasicSVNOperations(new BasicAuthPassThroughBasicSVNOperationsFactory(configuration.getUrl()));
    
    DeletedRevisionTracker tracker = new InMemoryDeletedRevisionTracker();
    Factory<PageStore> pageStoreFactory = new PerRequestPageStoreFactory(_searchEngine, tracker, operations);
    RequestScopedThreadLocalPageStore pageStore = new RequestScopedThreadLocalPageStore(pageStoreFactory);
    _plugins = new PluginsImpl(pageStore);
    _searchEngine.setPageStore(pageStore);
    ConfigPageCachingPageStore cachingPageStore = new ConfigPageCachingPageStore(pageStore);
    InternalLinker internalLinker = new InternalLinker(servletContext.getContextPath(), configuration.getGivenWikiName(), cachingPageStore);

    final WikiGraph wikiGraph = new WikiGraphImpl(cachingPageStore, _searchEngine);
    _renderer = new SvnWikiRenderer(new PageStoreConfiguration(pageStore), internalLinker, new Accessor<List<Macro>>() {
      public List<Macro> get() {
        List<Macro> macros = new ArrayList<Macro>(Arrays.<Macro>asList(new IncomingLinksMacro(wikiGraph), new OutgoingLinksMacro(wikiGraph), new SearchMacro(_searchEngine)));
        macros.addAll(_plugins.getImplementations(Macro.class));
        return macros;
      }
    });
    
    container.addComponent(tracker);
    container.addComponent(operations);
    container.addComponent(PageStore.class, pageStore);
    container.addComponent(CachingPageStore.class, cachingPageStore);
    
    container.addComponent(wikiGraph);
    container.addComponent(internalLinker);
    container.addComponent(renderedPageFactory);
    container.addComponent(_plugins);
    container.addComponent(_renderer);
    container.addComponent(_searchEngine);
      
    // Special pages.
    container.addComponent(SpecialPages.class, SpecialPagesImpl.class);
    container.addComponent(FindPageImpl.class);
    container.addComponent(OrphanedPagesImpl.class);
    container.addComponent(AllPagesImpl.class);
    container.addComponent(RecentChangesImpl.class);
    
    // Usual case handlers.
    container.addComponent(PageHandler.class, PageHandlerImpl.class);
    container.addComponent(RegularPage.class, RegularPageImpl.class);
    container.addComponent(GetRegularPage.class, GetRegularPageImpl.class);
    container.addComponent(EditorForPage.class, EditorForPageImpl.class);
    container.addComponent(SetPage.class, SetPageImpl.class);
    container.addComponent(History.class, HistoryImpl.class);

    // Attachments.
    container.addComponent(ListAttachments.class, ListAttachmentsImpl.class);
    container.addComponent(GetAttachment.class, GetAttachmentImpl.class);
    container.addComponent(UploadAttachment.class, UploadAttachmentImpl.class);
    container.addComponent(Attachments.class, AttachmentsImpl.class);
    
    // Allow plugin classes to depend on the core wiki API.
    _plugins.addPluginAccessibleComponent(pageStore);
    _plugins.addPluginAccessibleComponent(wikiGraph);
    _plugins.addPluginAccessibleComponent(_searchEngine);
    
    container.addComponent(ChangeNotificationDispatcherImpl.class);

    container.addComponent(WikiSession.class, this);
    container.addComponent(WikiHandlerImpl.class, WikiHandlerImpl.class);
  }

}
