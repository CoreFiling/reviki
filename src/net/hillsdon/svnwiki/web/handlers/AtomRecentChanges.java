package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.RequestBasedWikiUrls;
import net.hillsdon.svnwiki.web.RequestHandler;
import net.hillsdon.svnwiki.wiki.FeedWriter;

import org.xml.sax.SAXException;

public class AtomRecentChanges implements RequestHandler {

  private PageStore _store;

  public AtomRecentChanges(final PageStore pageStore) {
    _store = pageStore;
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException, ServletException, TransformerConfigurationException, SAXException {
    response.setContentType("application/atom+xml");
    FeedWriter.writeAtom(
        new RequestBasedWikiUrls(request),
        response.getWriter(), 
        _store.recentChanges(RecentChanges.RECENT_CHANGES_HISTORY_SIZE));
  }

}
