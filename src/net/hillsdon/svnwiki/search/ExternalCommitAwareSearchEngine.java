package net.hillsdon.svnwiki.search;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class ExternalCommitAwareSearchEngine implements SearchEngine {

  private PageStore _store;
  private final SearchEngine _delegate;
  private long _lastIndexedRevision = 0;

  public ExternalCommitAwareSearchEngine(final SearchEngine delegate) {
    _delegate = delegate;
  }
  
  public void setPageStore(final PageStore store) {
    _store = store;
  }
  
  public synchronized void index(final String path, final long revision, final String content) throws IOException, PageStoreException {
    _delegate.index(path, revision, content);
    _lastIndexedRevision = revision;
  }

  public Set<SearchMatch> search(final String query) throws IOException, QuerySyntaxException, PageStoreException {
    syncWithExternalCommits();
    return _delegate.search(query);
  }

  private synchronized void syncWithExternalCommits() throws PageStoreException, IOException {
    if (_store != null) {
      for (String page : _store.getChangedAfter(_lastIndexedRevision)) {
        PageInfo info = _store.get(page, -1);
        _delegate.index(info.getPath(), info.getRevision(), info.getContent());
        _lastIndexedRevision = Math.max(_lastIndexedRevision, info.getRevision());
      }
    }
  }

}
