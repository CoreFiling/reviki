package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;

public class Attachments extends PageRequestHandler {

  public Attachments(final PageStore pageStore) {
    super(pageStore);
  }

  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final String page) throws Exception {
    request.getRequestDispatcher("/WEB-INF/templates/Attachments.jsp").include(request, response);
  }

}
