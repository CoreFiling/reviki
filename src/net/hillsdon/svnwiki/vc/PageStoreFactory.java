package net.hillsdon.svnwiki.vc;

import javax.servlet.http.HttpServletRequest;

public interface PageStoreFactory {

  PageStore newInstance(HttpServletRequest request) throws PageStoreException;
  
}
