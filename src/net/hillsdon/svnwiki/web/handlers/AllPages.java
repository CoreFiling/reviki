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

public class AllPages extends PageRequestHandler {

  public AllPages(final PageStore store) {
    super(store);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException {
    List<PageReference> alphabetical = new ArrayList<PageReference>(getStore().list());
    Collections.sort(alphabetical);
    request.setAttribute("pageList", alphabetical);
    request.getRequestDispatcher("/WEB-INF/templates/AllPages.jsp").include(request, response);
  }

}
