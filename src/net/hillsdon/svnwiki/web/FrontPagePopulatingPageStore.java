package net.hillsdon.svnwiki.web;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class FrontPagePopulatingPageStore implements PageStore {

  private static final String FRONT_PAGE_CONTENT = "Welcome to ~~svnwiki~~!\n";

  private final PageStore _delegate;

  public FrontPagePopulatingPageStore(final PageStore delegate) {
    _delegate = delegate;
  }

  public PageInfo get(final String path) throws PageStoreException {
    PageInfo page = _delegate.get(path);
    if ("FrontPage".equals(path) && page.isNew()) {
      page = new PageInfo(page.getPath(), FRONT_PAGE_CONTENT, PageInfo.UNCOMMITTED, page.getLockedBy(), page.getLockToken());
    }
    return page;
  }

  public String[] list() throws PageStoreException {
    return _delegate.list();
  }

  public ChangeInfo[] recentChanges() throws PageStoreException {
    return _delegate.recentChanges();
  }

  public void set(final String path, final String lockToken, final long baseRevision, final String content) throws InterveningCommitException, PageStoreException {
    _delegate.set(path, lockToken, baseRevision, content);
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    return _delegate.tryToLock(path);
  }

  public void unlock(final String page, String lockToken) throws PageStoreException {
    _delegate.unlock(page, lockToken);
  }
  
}
