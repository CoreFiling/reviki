package net.hillsdon.svnwiki.search;

import java.io.IOException;

import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimpleDelegatingPageStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Intercepts page edits in order to update the search index.
 * 
 * @author mth
 */
public class SearchIndexPopulatingPageStore extends SimpleDelegatingPageStore {

  private static final Log LOG = LogFactory.getLog(SearchIndexPopulatingPageStore.class);

  private final SearchEngine _indexer;

  public SearchIndexPopulatingPageStore(final SearchEngine indexer, final PageStore delegate) {
    super(delegate);
    _indexer = indexer;
  }

  @Override
  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    long newRevision = super.set(ref, lockToken, baseRevision, content, commitMessage);
    try {
      _indexer.index(ref.getPath(), newRevision, content);
    }
    catch (IOException e) {
       LOG.error("Error adding to search index, skipping page: " + ref, e);
    }
    return newRevision;
  }
  
}
