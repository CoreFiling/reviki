package net.hillsdon.svnwiki.web;

import java.util.Collection;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Caches the set of pages forever.  This makes it only
 * suitable for a single request!
 * 
 * This lets us answer questions on page existence efficiently.
 * 
 * @author mth
 */
public class CachingPageStore extends DelegatingPageStore {

  private Collection<String> _cached = null;
  
  public CachingPageStore(PageStore delegate) {
    super(delegate);
  }

  @Override
  public Collection<String> list() throws PageStoreException {
    if (_cached == null) {
      _cached = super.list();
    }
    return _cached;
  }

}
