package net.hillsdon.svnwiki.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public class GetPage extends PageRequestHandler {

  public GetPage(final PageStoreFactory pageStoreFactory) {
    super(pageStoreFactory);
  }

  public void handlePage(HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException {
    PageInfo pageInfo = store.get(page);
    request.setAttribute("pageInfo", pageInfo);
    request.getRequestDispatcher("").forward(request, response);
  }

}
