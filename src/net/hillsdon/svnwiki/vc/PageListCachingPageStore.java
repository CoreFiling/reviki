package net.hillsdon.svnwiki.vc;

import java.util.Collection;

import net.hillsdon.svnwiki.web.DelegatingPageStore;

/**
 * Caches the set of pages forever.  This makes it only
 * suitable for a single request!
 * 
 * This lets us answer questions on page existence efficiently.
 * 
 * @author mth
 */
public class PageListCachingPageStore extends DelegatingPageStore {

  private Collection<PageReference> _cached = null;
  
  public PageListCachingPageStore(PageStore delegate) {
    super(delegate);
  }

  @Override
  public Collection<PageReference> list() throws PageStoreException {
    if (_cached == null) {
      _cached = super.list();
    }
    return _cached;
  }

}
