package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class EditorForPage extends PageRequestHandler {

  public EditorForPage(final PageStore store) {
    super(store);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException {
    PageInfo pageInfo = store.get(page);
    request.setAttribute("pageInfo", pageInfo);
    request.getRequestDispatcher("/WEB-INF/templates/EditPage.jsp").include(request, response);
  }

}
