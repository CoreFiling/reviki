package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
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
      FeedWriter.writeAtom(
          new RequestBasedWikiUrls(request),
          response.getWriter(), 
          _store.recentChanges(RecentChanges.RECENT_CHANGES_HISTORY_SIZE));
      return;
    }
    
    request.setAttribute("recentChanges", _store.recentChanges(RECENT_CHANGES_HISTORY_SIZE));
    request.getRequestDispatcher("/WEB-INF/templates/RecentChanges.jsp").include(request, response);  }
  }
