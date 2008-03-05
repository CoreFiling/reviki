package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;



/**
 * Delegates all functionality to given delegate.  Subclass to alter behaviour. 
 * 
 * @author mth
 */
public abstract class AbstractDelegatingPageStore implements PageStore {

  /**
   * @return The delegate to use.  This is called for each delegation.
   */
  protected abstract PageStore getDelegate();

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return getDelegate().get(ref, revision);
  }

  public Collection<PageReference> list() throws PageStoreException {
    return getDelegate().list();
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return getDelegate().recentChanges(limit);
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    return getDelegate().set(ref, lockToken, baseRevision, content, commitMessage);
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return getDelegate().tryToLock(ref);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreException {
    getDelegate().unlock(ref, lockToken);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return getDelegate().history(ref);
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    getDelegate().attach(ref, storeName, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return getDelegate().attachments(ref);
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    getDelegate().attachment(ref, attachment, revision, sink);
  }

  public Collection<PageReference> getChangedBetween(final long start, long end) throws PageStoreException {
    return getDelegate().getChangedBetween(start, end);
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().getLatestRevision();
  }

}
