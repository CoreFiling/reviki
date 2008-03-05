package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial implementation suitable for tests.
 * 
 * @author mth
 */
public class SimplePageStore implements PageStore {

  private Map<PageReference, PageInfo> _pages = new LinkedHashMap<PageReference, PageInfo>();
  
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    PageInfo page = _pages.get(ref);
    if (page == null) {
      page = new PageInfo(ref.getPath(), "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
      _pages.put(ref, page);
    }
    return page;
  }

  public Collection<PageReference> list() throws PageStoreException {
    return new ArrayList<PageReference>(_pages.values());
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return Collections.emptyList();
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreException {
    long revision = baseRevision + 1;
    PageInfo page = new PageInfo(ref.getPath(), content, revision, revision, null, null, null, null);
    _pages.put(ref, page);
    return revision;
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return get(ref, -1);
  }

  public void unlock(final PageReference ref, final String lockToken) {
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return Collections.emptyList();
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return Collections.emptySet();
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public Collection<PageReference> getChangedAfter(final long revision) {
    throw new UnsupportedOperationException();
  }
  
}