package net.hillsdon.svnwiki.vc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SVN provides no way to do svn log or similar on a delete URL
 * without knowing the last URL at which it existed.  They
 * recommend using svn log -v on the parent directory to find
 * that revision.  On a wiki with many revisions this is intolerably
 * slow as all the action is in a single directory.
 * 
 * We therefore keep up to date by reading the svn log an remember
 * revisions in which files were deleted.
 * 
 * This initial implementation is an in-memory cache so will repopulate
 * itself on every restart.
 * 
 * @author mth
 */
public class DeletionRevisionTracker {

  private final Map<String, ChangeInfo> _deletions = new ConcurrentHashMap<String, ChangeInfo>();
  private long _lastTracked;
 
  private void catchUp(final SVNHelper helper) throws PageStoreAuthenticationException, PageStoreException {
    long latest = helper.getLatestRevision();
    if (latest > _lastTracked) {
      for (ChangeInfo change : helper.log("", -1, false, _lastTracked, latest)) {
        final String page = change.getPage();
        if (page != null) {
          if (change.getChangeType() == ChangeType.DELETED) {
            _deletions.put(page, change);
          }
          else {
            _deletions.remove(page);
          }
        }
      }
    }
    _lastTracked = latest;
  }
  
  public ChangeInfo getChangeThatDeleted(final SVNHelper helper, final String path) throws PageStoreAuthenticationException, PageStoreException {
    catchUp(helper);
    return _deletions.get(path);
  }
  
}
