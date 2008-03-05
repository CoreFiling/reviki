package net.hillsdon.svnwiki.web;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import net.hillsdon.svnwiki.vc.AttachmentHistory;
import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.InterveningCommitException;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
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

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return _delegate.get(ref, revision);
  }

  public Collection<PageReference> list() throws PageStoreException {
    return _delegate.list();
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return _delegate.recentChanges(limit);
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    return _delegate.set(ref, lockToken, baseRevision, content, commitMessage);
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return _delegate.tryToLock(ref);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreException {
    _delegate.unlock(ref, lockToken);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return _delegate.history(ref);
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    _delegate.attach(ref, storeName, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return _delegate.attachments(ref);
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    _delegate.attachment(ref, attachment, revision, sink);
  }

  public Collection<PageReference> getChangedAfter(final long revision) throws PageStoreException {
    return _delegate.getChangedAfter(revision);
  }
  
}
