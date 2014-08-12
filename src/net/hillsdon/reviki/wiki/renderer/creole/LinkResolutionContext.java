package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;
import java.net.URISyntaxException;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class LinkResolutionContext {
  private final InternalLinker _internalLinker;
  private final PageStore _store;
  private final InterWikiLinker _interWikiLinker;
  private final PageReference _page;
  
  public LinkResolutionContext(final InternalLinker internalLinker, final InterWikiLinker interWikiLinker, final PageStore store) {
    _internalLinker = internalLinker;
    _interWikiLinker = interWikiLinker;
    _store = store;
    _page = null;
  }
  
  public LinkResolutionContext(final InternalLinker internalLinker, final InterWikiLinker interWikiLinker, final PageStore store, final PageReference page) {
    _internalLinker = internalLinker;
    _interWikiLinker = interWikiLinker;
    _store = store;
    _page = page;
  }

  public URI resolve(String wiki, String pageName, String revision) throws UnknownWikiException, URISyntaxException {
    if (wiki!=null) {
      return _interWikiLinker.uri(wiki, pageName, null);
    }
    if (pageName == null) {
      if (_page == null) {
        throw new IllegalStateException("LinkResolutionContext not initialised with a context page.");
      }
      pageName = _page.getName();
    }

    URI target = _internalLinker.uri(pageName);
    if(revision == null) {
      return target;
    }
    else {
      String oldQuery = target.getQuery();
      String newQuery = ((oldQuery == null) ? "" : oldQuery + "&") + "revision=" + revision;
      return new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(), target.getPath(), newQuery, target.getFragment());
    }
  }

  public URI resolve(String wiki, String pageName) throws UnknownWikiException, URISyntaxException {
    return resolve(wiki, pageName, null);
  }

  public boolean exists(PageReferenceImpl pageReferenceImpl) throws PageStoreException {
    return _store.exists(pageReferenceImpl);
  }

  public LinkResolutionContext derive(PageReference page) {
    return new LinkResolutionContext(_internalLinker, _interWikiLinker, _store, page);
  }
}
