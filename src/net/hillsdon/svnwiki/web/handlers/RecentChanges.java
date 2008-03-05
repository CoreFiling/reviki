package net.hillsdon.svnwiki.web.handlers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestBasedWikiUrls;
import net.hillsdon.svnwiki.web.common.RequestHandler;
import net.hillsdon.svnwiki.wiki.feeds.FeedWriter;

public class RecentChanges implements RequestHandler {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 30;

  private final PageStore _store;

  public RecentChanges(final PageStore store) {
    _store = store;
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if ("atom.xml".equals(path.next())) {
      response.setContentType("application/atom+xml");
      FeedWriter.writeAtom(new RequestBasedWikiUrls(request), response.getWriter(), getRecentChanges());
      return;
    }

    request.setAttribute("recentChanges", getRecentChanges());
    request.getRequestDispatcher("/WEB-INF/templates/RecentChanges.jsp").include(request, response);
  }

  private List<ChangeInfo> getRecentChanges() throws PageStoreException {
    return _store.recentChanges(RecentChanges.RECENT_CHANGES_HISTORY_SIZE);
  }

}
