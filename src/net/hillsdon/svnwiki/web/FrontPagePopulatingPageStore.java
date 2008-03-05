package net.hillsdon.svnwiki.web;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class FrontPagePopulatingPageStore extends DelegatingPageStore {

  private static final String FRONT_PAGE_CONTENT = "Welcome to ~~svnwiki~~!\n";

  public FrontPagePopulatingPageStore(final PageStore delegate) {
    super(delegate);
  }

  public PageInfo get(final String path, long revision) throws PageStoreException {
    PageInfo page = super.get(path, revision);
    if ("FrontPage".equals(path) && page.isNew()) {
      page = new PageInfo(page.getPath(), FRONT_PAGE_CONTENT, PageInfo.UNCOMMITTED, page.getLockedBy(), page.getLockToken());
    }
    return page;
  }

}
