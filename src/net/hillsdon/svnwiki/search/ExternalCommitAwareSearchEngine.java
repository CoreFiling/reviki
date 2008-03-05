package net.hillsdon.svnwiki.search;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class ExternalCommitAwareSearchEngine implements SearchEngine {

  private PageStore _store;
  private final SearchEngine _delegate;

  public ExternalCommitAwareSearchEngine(final SearchEngine delegate) {
    _delegate = delegate;
  }
  
  /**
   * Setter to avoid circularity.  If set we'll notice external commits otherwise just delegate.
   */
  public void setPageStore(final PageStore store) {
    _store = store;
  }
  
  public synchronized void index(final String path, final long revision, final String content) throws IOException, PageStoreException {
    _delegate.index(path, revision, content);
  }

  public Set<SearchMatch> search(final String query) throws IOException, QuerySyntaxException, PageStoreException {
    syncWithExternalCommits();
    return _delegate.search(query);
  }

  private synchronized void syncWithExternalCommits() throws PageStoreException, IOException {
    if (_store != null) {
      long latest = _store.getLatestRevision();
      long highestIndexed = _delegate.getHighestIndexedRevision();
      if (latest > highestIndexed) {
        for (PageReference ref : _store.getChangedBetween(highestIndexed + 1, latest)) {
          PageInfo info = _store.get(ref, latest);
          // Note we pass 'latest' as the revision here.  At the moment we get
          // back the revision of deleted pages as -2 which isn't such a good
          // thing to set our 'highest indexed revision' to...
          if (info.isNew()) {
            _delegate.delete(info.getPath(), latest);
          }
          else {
            _delegate.index(info.getPath(), latest, info.getContent());
          }
        }
      }
    }
  }

  public long getHighestIndexedRevision() throws IOException {
    return _delegate.getHighestIndexedRevision();
  }

  public void delete(final String path, final long revision) throws IOException {
    _delegate.delete(path, revision);
  }

  public String escape(final String in) {
    return _delegate.escape(in);
  }

}
