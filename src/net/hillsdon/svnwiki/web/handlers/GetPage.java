package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetPage extends PageRequestHandler {

  private GetRegularPage _regularPage;
  private RecentChanges _recentChanges;
  private AllPages _allPages;

  public GetPage(final PageStore pageStore, final SearchEngine searchEngine, final MarkupRenderer markupRenderer) {
    super(pageStore);
    _regularPage = new GetRegularPage(pageStore, markupRenderer, searchEngine);
    _recentChanges = new RecentChanges(pageStore);
    _allPages = new AllPages(pageStore);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final String page) throws Exception {
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
