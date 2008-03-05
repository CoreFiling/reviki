package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class RecentChanges extends PageRequestHandler {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 30;

  public RecentChanges(final PageStore pageStore) {
    super(pageStore);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final String page) throws PageStoreException, IOException, ServletException {
    request.setAttribute("recentChanges", getStore().recentChanges(RECENT_CHANGES_HISTORY_SIZE));
    request.getRequestDispatcher("/WEB-INF/templates/RecentChanges.jsp").include(request, response);
  }

}
