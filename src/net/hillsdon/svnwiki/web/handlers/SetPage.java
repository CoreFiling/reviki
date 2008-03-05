/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getRequiredString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

public class SetPage implements PageRequestHandler {

  private static final String PARAM_FROM_PAGE = "fromPage";
  private static final String PARAM_FROM_REVISION = "fromRevision";

  static final String PARAM_CONTENT = "content";
  private static final String PARAM_BASE_REVISION = "baseRevision";
  private static final String PARAM_LOCK_TOKEN = "lockToken";
  static final String PARAM_COMMIT_MESSAGE = "description";
  private static final String PARAM_MINOR_EDIT = "minorEdit";
  
  private static final String CRLF = "\r\n";
  
  private final PageStore _store;

  private static String createLinkingCommitMessage(final HttpServletRequest request) {
    boolean minorEdit = request.getParameter(PARAM_MINOR_EDIT) != null;
    String commitMessage = request.getParameter(PARAM_COMMIT_MESSAGE);
    if (commitMessage == null || commitMessage.trim().length() == 0) {
      commitMessage = ChangeInfo.NO_COMMENT_MESSAGE_TAG;
    }
    return (minorEdit ? ChangeInfo.MINOR_EDIT_MESSAGE_TAG : "") + commitMessage + "\n" + request.getRequestURL();
  }

  public SetPage(final PageStore store) {
    _store = store;
  }

  public void handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if (request.getParameter("save") != null) {
      String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
      long baseRevision = getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
      String content = getRequiredString(request, PARAM_CONTENT);
      if (!content.endsWith(CRLF)) {
        content = content + CRLF;
      }
      _store.set(page, lockToken, baseRevision, content, createLinkingCommitMessage(request));
    }
    else if (request.getParameter("copy") != null) {
      final String fromPage = getRequiredString(request, PARAM_FROM_PAGE);
      final long fromRevision = getLong(PARAM_FROM_REVISION, getRequiredString(request, PARAM_FROM_REVISION));
      _store.copy(new PageReference(fromPage), fromRevision, page, createLinkingCommitMessage(request));
    }
    else if (request.getParameter("unlock") != null) {
      String lockToken = request.getParameter(PARAM_LOCK_TOKEN);
      // New pages don't have a lock.
      if (lockToken != null && lockToken.length() > 0) {
        _store.unlock(page, lockToken);
      }
    }
    response.sendRedirect(request.getRequestURI());
  }

}
