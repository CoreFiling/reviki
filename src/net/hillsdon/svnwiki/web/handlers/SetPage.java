package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.InvalidInputException;

public class SetPage extends PageRequestHandler {

  private static final String CRLF = "\r\n";

  private static String getRequiredString(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String value = request.getParameter(parameter);
    if (value == null) {
      throw new InvalidInputException(String.format("'%s' required.", parameter));
    }
    return value;
  }

  private static long getRequiredLong(final HttpServletRequest request, final String parameter) throws InvalidInputException {
    String baseRevisionString = getRequiredString(request, parameter);
    try {
      return Long.parseLong(baseRevisionString);
    }
    catch (NumberFormatException ex) {
      throw new InvalidInputException(String.format("'%s' invalid.", parameter));
    }
  }
  
  public SetPage(final PageStore store) {
    super(store);
  }

  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    String lockToken = getRequiredString(request, "lockToken");
    if ("Save".equals(request.getParameter("action"))) {
      long baseRevision = getRequiredLong(request, "baseRevision");
      String content = getRequiredString(request, "content");
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
