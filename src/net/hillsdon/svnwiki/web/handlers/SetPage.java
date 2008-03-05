package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;

public class SetPage extends PageRequestHandler {

  private static final String CRLF = "\r\n";

  public SetPage(final PageStore store) {
    super(store);
  }

  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    String lockToken = RequestParameterReaders.getRequiredString(request, "lockToken");
    if ("Save".equals(request.getParameter("action"))) {
      long baseRevision = RequestParameterReaders.getRequiredLong(request, "baseRevision");
      String content = RequestParameterReaders.getRequiredString(request, "content");
      if (!content.endsWith(CRLF)) {
        content = content + CRLF;
      }
      store.set(page, lockToken, baseRevision, content);
    }
    else {
      store.unlock(page, lockToken);
    }
    response.sendRedirect(request.getRequestURI());
  }

}
