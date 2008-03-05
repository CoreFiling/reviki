package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetPage extends PageRequestHandler {

  private GetRegularPage _regularPage;
  private RecentChanges _recentChanges;
  private AllPages _allPages;

  public GetPage(final PageStoreFactory pageStoreFactory, final MarkupRenderer markupRenderer) {
    super(pageStoreFactory);
    _regularPage = new GetRegularPage(pageStoreFactory, markupRenderer);
    _recentChanges = new RecentChanges(pageStoreFactory);
    _allPages = new AllPages(pageStoreFactory);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    if ("RecentChanges".equals(page)) {
      _recentChanges.handle(request, response);
    }
    else if ("AllPages".equals(page)) {
      _allPages.handle(request, response);
    }
    else {
      _regularPage.handle(request, response);
    }
  }

}
