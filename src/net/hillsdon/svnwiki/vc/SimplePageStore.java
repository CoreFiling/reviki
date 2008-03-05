package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
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

  private Map<String, PageInfo> _pages = new LinkedHashMap<String, PageInfo>();
  
  public PageInfo get(final String path, final long revision) throws PageStoreException {
    PageInfo page = _pages.get(path);
    if (page == null) {
      page = new PageInfo(path, "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
      _pages.put(path, page);
    }
    return page;
  }

  public Collection<String> list() throws PageStoreException {
    return _pages.keySet();
  }

  public List<ChangeInfo> recentChanges(int limit) throws PageStoreException {
    return Collections.emptyList();
  }

  public void set(final String path, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreException {
    long revision = baseRevision + 1;
    PageInfo page = new PageInfo(path, content, revision, revision, null, null, null, null);
    _pages.put(path, page);
  }

  public PageInfo tryToLock(final String path) throws PageStoreException {
    return get(path, -1);
  }

  public void unlock(final String page, final String lockToken) {
  }

  public List<ChangeInfo> history(final String path) throws PageStoreException {
    return Collections.emptyList();
  }

  public void attach(final String page, final String storeName, long baseRevision, final InputStream in, String commitMessage) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public Collection<AttachmentHistory> attachments(final String page) throws PageStoreException {
    return Collections.emptySet();
  }

  public void attachment(final String page, final String attachment, long revision, final ContentTypedSink sink) throws PageStoreException {
    throw new UnsupportedOperationException();
  }
  
}