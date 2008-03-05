package net.hillsdon.svnwiki.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public class EditorForPage extends PageRequestHandler {

  public EditorForPage(final PageStoreFactory pageStoreFactory) {
    super(pageStoreFactory);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException {
    PageInfo pageInfo = store.get(page);
    request.setAttribute("pageInfo", pageInfo);
    request.getRequestDispatcher("/WEB-INF/templates/EditPage.jsp").include(request, response);
  }

}
