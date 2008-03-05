package net.hillsdon.svnwiki.web;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.vc.AttachmentHistory;
import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

/**
 * Allow us to pass a PageStore into various objects but
 *  a) use authentication from the current request
 *  b) work with the thread-safety limitations of 
 *     {@link org.tmatesoft.svn.core.io.SVNRepository}.
 * 
 * @author mth
 */
public final class RequestScopedThreadLocalPageStore implements PageStore {

  private final ThreadLocal<PageStore> _threadLocal = new ThreadLocal<PageStore>();
  private final PageStoreFactory _factory;
  
  public RequestScopedThreadLocalPageStore(final PageStoreFactory factory) {
    _factory = factory;
  }
  
  public void create(final HttpServletRequest request) throws PageStoreException {
    _threadLocal.set(_factory.newInstance(request));
  }
  
  public void destroy() {
    _threadLocal.set(null);
  }
  
  private PageStore get() {
    return _threadLocal.get();
  }
  
  public PageInfo get(final String path, final long revision) throws PageStoreException {
    return get().get(path, revision);
  }

  public Collection<String> list() throws PageStoreException {
    return get().list();
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return get().recentChanges(limit);
  }

  public long set(final String path, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreException {
    return get().set(path, lockToken, baseRevision, content, commitMessage);
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    return get().tryToLock(path);
  }

  public void unlock(final String page, final String lockToken) throws PageStoreException {
    get().unlock(page, lockToken);
  }

  public List<ChangeInfo> history(final String path) throws PageStoreException {
    return get().history(path);
  }

  public void attach(final String page, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    get().attach(page, storeName, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final String page) throws PageStoreException {
    return get().attachments(page);
  }

  public void attachment(final String page, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    get().attachment(page, attachment, revision, sink);
  }

  public Collection<String> getChangedAfter(final long revision) throws PageStoreException {
    return get().getChangedAfter(revision);
  }

}
