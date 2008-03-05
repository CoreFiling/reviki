package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.RequestAttributes;

public class EditorForPage extends PageRequestHandler {

  public EditorForPage(final PageStore store) {
    super(store);
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException {
    PageInfo pageInfo = getStore().tryToLock(page);
    request.setAttribute("pageInfo", pageInfo);
    if (!pageInfo.lockedByUserIfNeeded((String) request.getAttribute(RequestAttributes.USERNAME))) {
      request.setAttribute("flash", "Could not lock the page.");
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").include(request, response);
      return;
    }
    else {
      request.getRequestDispatcher("/WEB-INF/templates/EditPage.jsp").include(request, response);
    }
  }

}
