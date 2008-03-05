package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getLong;
import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getRequiredString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.ConsumedPath;

public class SetPage extends PageRequestHandler {

  // Value of the submit element, 'Save' or 'Cancel'.
  private static final String PARAM_ACTION = "action";
  static final String PARAM_CONTENT = "content";
  private static final String PARAM_BASE_REVISION = "baseRevision";
  private static final String PARAM_LOCK_TOKEN = "lockToken";
  private static final String PARAM_COMMIT_MESSAGE = "description";
  
  private static final String DEFAULT_COMMIT_MESSAGE = "[svnwiki commit]";
  private static final String CRLF = "\r\n";

  private static String createLinkingCommitMessage(final HttpServletRequest request) {
    String commitMessage = request.getParameter(PARAM_COMMIT_MESSAGE);
    if (commitMessage == null || commitMessage.trim().length() == 0) {
      commitMessage = DEFAULT_COMMIT_MESSAGE;
    }
    return commitMessage + "\n" + request.getRequestURL();
  }

  public SetPage(final PageStore store) {
    super(store);
  }

  @Override
  public void handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
    if ("Save".equals(request.getParameter(PARAM_ACTION))) {
      long baseRevision = getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
      String content = getRequiredString(request, PARAM_CONTENT);
      if (!content.endsWith(CRLF)) {
        content = content + CRLF;
      }
      getStore().set(page, lockToken, baseRevision, content, createLinkingCommitMessage(request));
    }
    else {
      // New pages don't have a lock.
      if (lockToken.length() > 0) {
        getStore().unlock(page, lockToken);
      }
    }
    response.sendRedirect(request.getRequestURI());
  }

}
