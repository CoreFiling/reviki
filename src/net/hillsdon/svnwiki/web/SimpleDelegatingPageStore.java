package net.hillsdon.svnwiki.web;


import net.hillsdon.svnwiki.vc.PageStore;

/**
 * A delegating page store that delegates to a given page store.
 * 
 * @author mth
 */
public class SimpleDelegatingPageStore extends AbstractDelegatingPageStore implements PageStore {

  private final PageStore _delegate;

  public SimpleDelegatingPageStore(final PageStore delegate) {
    _delegate = delegate;
  }
  
  @Override
  protected PageStore getDelegate() {
    return _delegate;
  }
  
}
