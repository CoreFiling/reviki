package net.hillsdon.svnwiki.search;

import java.io.IOException;

import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.DelegatingPageStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Intercepts page edits in order to update the search index.
 * 
 * @author mth
 */
public class SearchIndexPopulatingPageStore extends DelegatingPageStore {

  private static final Log LOG = LogFactory.getLog(SearchIndexPopulatingPageStore.class);

  private final SearchIndexer _indexer;

  public SearchIndexPopulatingPageStore(final SearchIndexer indexer, final PageStore delegate) {
    super(delegate);
    _indexer = indexer;
  }

  @Override
  public void set(String path, String lockToken, long baseRevision, String content, String commitMessage) throws InterveningCommitException, PageStoreException {
    super.set(path, lockToken, baseRevision, content, commitMessage);
    try {
      _indexer.index(path, content);
    }
    catch (IOException e) {
       LOG.error("Error adding to search index, skipping page: " + path, e);
    }
  }
  
}
