package net.hillsdon.svnwiki.vc;

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
  
  public PageInfo get(final String path) throws PageStoreException {
    PageInfo page = _pages.get(path);
    if (page == null) {
      page = new PageInfo(path, "", PageInfo.UNCOMMITTED, null, null);
      _pages.put(path, page);
    }
    return page;
  }

  public Collection<String> list() throws PageStoreException {
    return _pages.keySet();
  }

  public List<ChangeInfo> recentChanges() throws PageStoreException {
    return Collections.emptyList();
  }

  public void set(final String path, String lockToken, final long baseRevision, final String content) throws PageStoreException {
    PageInfo page = new PageInfo(path, content, baseRevision + 1, null, null);
    _pages.put(path, page);
  }

  public PageInfo tryToLock(String path) throws PageStoreException {
    return get(path);
  }

  public void unlock(String page, String lockToken) {
  }
  
}