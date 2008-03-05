package net.hillsdon.svnwiki.web;

import java.util.Collection;
import java.util.List;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Delegates all functionality to given delegate.  Subclass to alter behaviour. 
 * 
 * @author mth
 */
public class DelegatingPageStore implements PageStore {

  private final PageStore _delegate;

  public DelegatingPageStore(PageStore delegate) {
    _delegate = delegate;
  }
  
  protected PageStore getDelegate() {
    return _delegate;
  }

  public PageInfo get(String path, long revision) throws PageStoreException {
    return _delegate.get(path, revision);
  }

  public Collection<String> list() throws PageStoreException {
    return _delegate.list();
  }

  public List<ChangeInfo> recentChanges() throws PageStoreException {
    return _delegate.recentChanges();
  }

  public void set(String path, String lockToken, long baseRevision, String content) throws InterveningCommitException, PageStoreException {
    _delegate.set(path, lockToken, baseRevision, content);
  }

  public PageInfo tryToLock(String path) throws PageStoreException {
    return _delegate.tryToLock(path);
  }

  public void unlock(String page, String lockToken) throws PageStoreException {
    _delegate.unlock(page, lockToken);
  }
  
}
