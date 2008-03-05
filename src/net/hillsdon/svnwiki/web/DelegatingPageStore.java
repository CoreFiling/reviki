package net.hillsdon.svnwiki.web;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreEntry;
import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Delegates all functionality to given delegate.  Subclass to alter behaviour. 
 * 
 * @author mth
 */
public class DelegatingPageStore implements PageStore {

  private final PageStore _delegate;

  public DelegatingPageStore(final PageStore delegate) {
    _delegate = delegate;
  }
  
  protected PageStore getDelegate() {
    return _delegate;
  }

  public PageInfo get(final String path, final long revision) throws PageStoreException {
    return _delegate.get(path, revision);
  }

  public Collection<String> list() throws PageStoreException {
    return _delegate.list();
  }

  public List<ChangeInfo> recentChanges() throws PageStoreException {
    return _delegate.recentChanges();
  }

  public void set(final String path, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    _delegate.set(path, lockToken, baseRevision, content, commitMessage);
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    return _delegate.tryToLock(path);
  }

  public void unlock(final String page, final String lockToken) throws PageStoreException {
    _delegate.unlock(page, lockToken);
  }

  public List<ChangeInfo> history(final String path) throws PageStoreException {
    return _delegate.history(path);
  }

  public void attach(final String page, final String storeName, long baseRevision, final InputStream in, String commitMessage) throws PageStoreException {
    _delegate.attach(page, storeName, baseRevision, in, commitMessage);
  }

  public Collection<PageStoreEntry> attachments(final String page) throws PageStoreException {
    return _delegate.attachments(page);
  }

  public void attachment(final String page, final String attachment, final ContentTypedSink sink) throws PageStoreException {
    _delegate.attachment(page, attachment, sink);
  }
  
}
