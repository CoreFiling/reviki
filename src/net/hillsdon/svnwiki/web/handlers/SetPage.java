package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.InvalidInputException;

public class SetPage extends PageRequestHandler {

  public SetPage(final PageStore store) {
    super(store);
  }

  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    String lockToken = request.getParameter("lockToken");
    if (lockToken == null) {
      throw new InvalidInputException("'lockToken' required.");
    }
    if ("Save".equals(request.getParameter("action"))) {
      String baseRevisionString = request.getParameter("baseRevision");
      if (baseRevisionString == null) {
        throw new InvalidInputException("'baseRevision' required.");
      }
      long baseRevision;
      try {
        baseRevision = Long.parseLong(baseRevisionString);
      }
      catch (NumberFormatException ex) {
        throw new InvalidInputException("'baseRevision' invalid.");
      }

      String content = request.getParameter("content");
      if (content == null) {
        throw new InvalidInputException("'content' required.");
      }
      if (!content.endsWith("\n")) {
        content = content + "\n";
      }

      store.set(page, lockToken, baseRevision, content);
    }
    else {
      store.unlock(page, lockToken);
    }
    response.sendRedirect(request.getRequestURI());
  }

}
