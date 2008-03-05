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
import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getString;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ChangeInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.RequestBasedWikiUrls;

public class SetPage implements PageRequestHandler {

  public static final String SUBMIT_SAVE = "save";
  public static final String SUBMIT_COPY = "copy";
  public static final String SUBMIT_RENAME = "rename";
  public static final String SUBMIT_UNLOCK = "unlock";

  public static final String PARAM_TO_PAGE = "toPage";
  public static final String PARAM_FROM_PAGE = "fromPage";
  public static final String PARAM_CONTENT = "content";
  public static final String PARAM_BASE_REVISION = "baseRevision";
  public static final String PARAM_LOCK_TOKEN = "lockToken";
  public static final String PARAM_COMMIT_MESSAGE = "description";
  public static final String PARAM_MINOR_EDIT = "minorEdit";

  private static String createLinkingCommitMessage(final HttpServletRequest request) {
    boolean minorEdit = request.getParameter(PARAM_MINOR_EDIT) != null;
    String commitMessage = request.getParameter(PARAM_COMMIT_MESSAGE);
    if (commitMessage == null || commitMessage.trim().length() == 0) {
      commitMessage = ChangeInfo.NO_COMMENT_MESSAGE_TAG;
    }
    return (minorEdit ? ChangeInfo.MINOR_EDIT_MESSAGE_TAG : "") + commitMessage + "\n" + request.getRequestURL();
  }
  
  private static final String CRLF = "\r\n";
  
  private final PageStore _store;

  public SetPage(final PageStore store) {
    _store = store;
  }

  public void handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if (request.getParameter(SUBMIT_SAVE) != null) {
      String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
      String content = getRequiredString(request, PARAM_CONTENT);
      if (!content.endsWith(CRLF)) {
        content = content + CRLF;
      }
      _store.set(page, lockToken, getBaseRevision(request), content, createLinkingCommitMessage(request));
    }
    else if (request.getParameter(SUBMIT_COPY) != null) {
      final String fromPage = getString(request, PARAM_FROM_PAGE);
      final String toPage = getString(request, PARAM_TO_PAGE);
      if (!(fromPage == null ^ toPage == null)) {
        throw new InvalidInputException("'copy' requires one of toPage, fromPage");
      }
      final PageReference toRef;
      final PageReference fromRef;
      if (fromPage != null) {
        fromRef = new PageReference(fromPage);
        toRef = page;
      }
      else {
        fromRef = page;
        toRef = new PageReference(toPage);
      }
      _store.copy(fromRef, -1, toRef, createLinkingCommitMessage(request));
      response.sendRedirect(RequestBasedWikiUrls.get(request).page(toPage));
      return;
    }
    else if (request.getParameter(SUBMIT_RENAME) != null) {
      final String toPage = getRequiredString(request, PARAM_TO_PAGE);
      _store.rename(page, new PageReference(toPage), -1, createLinkingCommitMessage(request));
      response.sendRedirect(RequestBasedWikiUrls.get(request).page(toPage));
      return;
    }
    else if (request.getParameter(SUBMIT_UNLOCK) != null) {
      String lockToken = request.getParameter(PARAM_LOCK_TOKEN);
      // New pages don't have a lock.
      if (lockToken != null && lockToken.length() > 0) {
        _store.unlock(page, lockToken);
      }
    }
    response.sendRedirect(request.getRequestURI());
  }

  private Long getBaseRevision(final HttpServletRequest request) throws InvalidInputException {
    return getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
  }

}
