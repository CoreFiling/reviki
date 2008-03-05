package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

/**
 * TODO: Convert to interface.
 * 
 * @author mth
 */
public abstract class PageRequestHandler {

  private PageStore _store;

  public PageRequestHandler(final PageStore store) {
    _store = store;
  }
  
  public PageStore getStore() {
    return _store;
  }
  
  public abstract void handlePage(ConsumedPath path, HttpServletRequest request, HttpServletResponse response, PageReference page) throws Exception;

}
