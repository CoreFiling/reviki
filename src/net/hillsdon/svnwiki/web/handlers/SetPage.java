package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getLong;
import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getRequiredString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;

public class SetPage extends PageRequestHandler {

  private static final String PARAM_CONTENT = "content";
  private static final String PARAM_BASE_REVISION = "baseRevision";
  private static final String PARAM_LOCK_TOKEN = "lockToken";
  private static final String CRLF = "\r\n";

  public SetPage(final PageStore store) {
    super(store);
  }

  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
    if ("Save".equals(request.getParameter("action"))) {
      long baseRevision = getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
      String content = getRequiredString(request, PARAM_CONTENT);
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
