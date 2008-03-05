package net.hillsdon.svnwiki.vc;

import java.util.LinkedHashMap;
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
      page = new PageInfo(path, "", PageInfo.UNCOMMITTED);
      _pages.put(path, page);
    }
    return page;
  }

  public String[] list() throws PageStoreException {
    return _pages.keySet().toArray(new String[_pages.size()]);
  }

  public ChangeInfo[] recentChanges() throws PageStoreException {
    return new ChangeInfo[0];
  }

  public void set(final String path, final long baseRevision, final String content) throws PageStoreException {
    PageInfo page = new PageInfo(path, content, baseRevision + 1);
    _pages.put(path, page);
  }
  
}