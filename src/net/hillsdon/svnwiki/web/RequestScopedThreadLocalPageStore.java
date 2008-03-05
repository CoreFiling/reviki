package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public class RequestScopedThreadLocalPageStore implements PageStore {

  private final ThreadLocal<PageStore> _threadLocal = new ThreadLocal<PageStore>();
  private final PageStoreFactory _factory;
  
  public RequestScopedThreadLocalPageStore(PageStoreFactory factory) {
    _factory = factory;
  }
  
  public void create(HttpServletRequest request) throws PageStoreException {
    _threadLocal.set(_factory.newInstance(request));
  }
  
  public void destroy() {
    _threadLocal.set(null);
  }
  
  private PageStore get() {
    return _threadLocal.get();
  }
  
  @Override
  public PageInfo get(String path) throws PageStoreException {
    return get().get(path);
  }

  @Override
  public String[] list() throws PageStoreException {
    return get().list();
  }

  @Override
  public String[] recentChanges() throws PageStoreException {
    return get().recentChanges();
  }

  @Override
  public void set(String path, long baseRevision, String content) throws PageStoreException {
    get().set(path, baseRevision, content);
  }

}
