package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.RequestBasedWikiUrls;
import net.hillsdon.svnwiki.wiki.FeedWriter;

import org.xml.sax.SAXException;

public class RecentChanges extends PageRequestHandler {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 30;
  
  public RecentChanges(final PageStore pageStore) {
    super(pageStore);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, TransformerConfigurationException, SAXException {
    if (request.getRequestURI().endsWith("/atom.xml")) {
      response.setContentType("application/atom+xml");
      FeedWriter.writeAtom(
          new RequestBasedWikiUrls(request),
          response.getWriter(), 
          getStore().recentChanges(RecentChanges.RECENT_CHANGES_HISTORY_SIZE));
      return;
    }
    
    request.setAttribute("recentChanges", getStore().recentChanges(RECENT_CHANGES_HISTORY_SIZE));
    request.getRequestDispatcher("/WEB-INF/templates/RecentChanges.jsp").include(request, response);
  }

}
