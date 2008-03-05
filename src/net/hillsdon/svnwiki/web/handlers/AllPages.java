package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class AllPages extends PageRequestHandler {

  public AllPages(final PageStore store) {
    super(store);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final String page) throws PageStoreException, IOException, ServletException {
    List<String> alphabetical = new ArrayList<String>(getStore().list());
    Collections.sort(alphabetical);
    request.setAttribute("pageList", alphabetical);
    request.getRequestDispatcher("/WEB-INF/templates/AllPages.jsp").include(request, response);
  }

}
