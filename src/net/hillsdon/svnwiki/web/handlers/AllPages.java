package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.ConsumedPath;
import net.hillsdon.svnwiki.web.RequestHandler;

public class AllPages implements RequestHandler {

  private final PageStore _store;

  public AllPages(final PageStore store) {
    _store = store;
  }

  public void handle(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException, ServletException {
    List<PageReference> alphabetical = new ArrayList<PageReference>(_store.list());
    Collections.sort(alphabetical);
    request.setAttribute("pageList", alphabetical);
    request.getRequestDispatcher("/WEB-INF/templates/AllPages.jsp").include(request, response);
  }

}
