package net.hillsdon.reviki.vc;

/**
 * This store is permitted to cache.
 * 
 * Depend on one of these if it is not vital that the retrieved
 * data is in sync with the SVN repository.
 * 
 * @author mth
 */
public interface CachingPageStore extends PageStore {

  /**
   * @return The underlying page store that non-caching access is delegated to.
   */
  PageStore getUnderlying();
  
}
