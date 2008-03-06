package net.hillsdon.svnwiki.web.vcintegration;

import net.hillsdon.fij.core.Factory;
import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.search.SearchIndexPopulatingPageStore;
import net.hillsdon.svnwiki.vc.BasicSVNOperations;
import net.hillsdon.svnwiki.vc.DeletedRevisionTracker;
import net.hillsdon.svnwiki.vc.PageListCachingPageStore;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.SVNPageStore;

public class PerRequestPageStoreFactory implements Factory<PageStore> {

  private final SearchEngine _indexer;
  private final DeletedRevisionTracker _tracker;
  private final BasicSVNOperations _operations;

  public PerRequestPageStoreFactory(final SearchEngine indexer, final DeletedRevisionTracker tracker, final BasicSVNOperations operations) {
    _indexer = indexer;
    _tracker = tracker;
    _operations = operations;
  }
  
  public PageStore newInstance() {
    return new SearchIndexPopulatingPageStore(_indexer, new PageListCachingPageStore(new SpecialPagePopulatingPageStore(new SVNPageStore(_tracker, _operations)))); 
  }
}
