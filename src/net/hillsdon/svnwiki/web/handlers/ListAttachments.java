package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

public class ListAttachments extends PageRequestHandler {

  public ListAttachments(final PageStore pageStore) {
    super(pageStore);
  }

  @Override
  public void handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    request.setAttribute("attachments", getStore().attachments(page));
    request.getRequestDispatcher("/WEB-INF/templates/Attachments.jsp").include(request, response);
  }

}
