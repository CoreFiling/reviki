package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.FeedWriter;

import org.xml.sax.SAXException;

public class AtomRecentChanges extends PageRequestHandler {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 30;

  public AtomRecentChanges(final PageStore pageStore) {
    super(pageStore);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, TransformerConfigurationException, SAXException {
    response.setContentType("application/atom+xml");
    FeedWriter.writeAtom(response.getWriter(), getStore().recentChanges(RECENT_CHANGES_HISTORY_SIZE));
  }

}
