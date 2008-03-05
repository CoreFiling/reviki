package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetPage extends PageRequestHandler {

  private final GetRegularPage _regularPage;
  private final RecentChanges _recentChanges;
  private final AllPages _allPages;
  private final Search _search;

  public GetPage(final PageStore pageStore, final SearchEngine searchEngine, final MarkupRenderer markupRenderer) {
    super(pageStore);
    _regularPage = new GetRegularPage(pageStore, markupRenderer, searchEngine);
    _recentChanges = new RecentChanges(pageStore);
    _allPages = new AllPages(pageStore);
    _search = new Search(pageStore, searchEngine);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if ("RecentChanges".equals(page.getPath())) {
      _recentChanges.handle(request, response);
    }
    else if ("AllPages".equals(page.getPath())) {
      _allPages.handle(request, response);
    }
    else if ("FindPage".equals(page.getPath())) {
      _search.handle(request, response);
    }
    else {
      _regularPage.handle(request, response);
    }
  }

}
