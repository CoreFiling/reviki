package net.hillsdon.svnwiki.vc;

import net.hillsdon.fij.collections.DelegatingTransformMap;
import net.hillsdon.fij.collections.TransformMap;
import net.hillsdon.fij.core.Transform;

/**
 * Partial implementation suitable for tests.
 * 
 * @author mth
 */
public class SimplePageStore implements PageStore {

  private TransformMap<String, PageInfo> _pages = new DelegatingTransformMap<String, PageInfo>(new Transform<PageInfo, String>() {
    @Override
    public String transform(final PageInfo in) {
      return in.getPath();
    }
  });
  
  
  @Override
  public PageInfo get(final String path) throws PageStoreException {
    PageInfo page = _pages.get(path);
    if (page == null) {
      page = new PageInfo(path, "", PageInfo.UNCOMMITTED);
      _pages.put(page);
    }
    return page;
  }

  @Override
  public String[] list() throws PageStoreException {
    return _pages.keySet().toArray(new String[_pages.size()]);
  }

  @Override
  public String[] recentChanges() throws PageStoreException {
    return new String[0];
  }

  @Override
  public void set(final String path, final long baseRevision, final String content) throws PageStoreException {
    PageInfo page = new PageInfo(path, content, baseRevision + 1);
    _pages.put(page);
  }
  
}