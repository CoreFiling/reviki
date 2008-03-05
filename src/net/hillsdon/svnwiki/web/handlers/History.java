package net.hillsdon.svnwiki.web.handlers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

public class History implements PageRequestHandler {

  private final PageStore _store;

  public History(final PageStore store) {
    _store = store;
  }

  public void handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    List<ChangeInfo> changes = _store.history(page);
    request.setAttribute("changes", changes);
    request.getRequestDispatcher("/WEB-INF/templates/History.jsp").include(request, response);
  }

}
