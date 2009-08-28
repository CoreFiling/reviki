/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.web.pages.impl;

import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRequiredString;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getString;
import static net.hillsdon.reviki.web.common.ViewTypeConstants.CTYPE_ATOM;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ConflictException;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.LostLockException;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.PageRevisionReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.RequestParameterReaders;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.handlers.RawPageView;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.pages.DiffGenerator;
import net.hillsdon.reviki.web.redirect.RedirectToPageView;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class DefaultPageImpl implements DefaultPage {

  public static final String ATTR_PREVIEW = "preview";

  public static final String SUBMIT_SAVE = "save";

  public static final String SUBMIT_COPY = "copy";

  public static final String SUBMIT_RENAME = "rename";

  public static final String SUBMIT_UNLOCK = "unlock";

  public static final String SUBMIT_PREVIEW = ATTR_PREVIEW;

  public static final String PARAM_TO_PAGE = "toPage";

  public static final String PARAM_FROM_PAGE = "fromPage";

  public static final String PARAM_CONTENT = "content";

  public static final String PARAM_BASE_REVISION = "baseRevision";

  public static final String PARAM_LOCK_TOKEN = "lockToken";

  public static final String PARAM_COMMIT_MESSAGE = "description";

  public static final String PARAM_MINOR_EDIT = "minorEdit";

  public static final String PARAM_DIFF_REVISION = "diff";

  public static final String PARAM_REVISION = "revision";

  public static final String PARAM_ATTACHMENT_NAME = "attachmentName";

  public static final String PARAM_SESSION_ID = "sessionId";

  public static final String ATTR_PAGE_INFO = "pageInfo";

  public static final String ATTR_BACKLINKS = "backlinks";

  public static final String ATTR_BACKLINKS_LIMITED = "backlinksLimited";

  public static final String ATTR_RENDERED_CONTENTS = "renderedContents";

  public static final String ATTR_MARKED_UP_DIFF = "markedUpDiff";

  public static final String ATTR_DIFF_START_REV = "diffStartRev";

  public static final String ATTR_DIFF_END_REV = "diffEndRev";

  /**
   * Because the diff view may be looking at renamed pages.
   */
  public static final String ATTR_DIFF_TITLE = "diffTitle";

  public static final String ATTR_SHOW_REV = "showHeadRev";

  public static final String ATTR_SESSION_ID = "sessionId";

  public static final String ERROR_NO_FILE = "Please browse to a non-empty file to upload.";

  public static final String ERROR_SESSION_EXPIRED = "Your session has expired. Please try again.";

  public static final int MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY = 15;

  private final CachingPageStore _store;

  private final MarkupRenderer _renderer;

  private final WikiGraph _graph;

  private final DiffGenerator _diffGenerator;

  private final WikiUrls _wikiUrls;

  private final FeedWriter _feedWriter;

  private final WikiConfiguration _configuration;

  public DefaultPageImpl(final WikiConfiguration configuration, final CachingPageStore store, final MarkupRenderer renderer, final WikiGraph graph, final DiffGenerator diffGenerator, final WikiUrls wikiUrls, final FeedWriter feedWriter) {
    _configuration = configuration;
    _store = store;
    _renderer = renderer;
    _graph = graph;
    _diffGenerator = diffGenerator;
    _wikiUrls = wikiUrls;
    _feedWriter = feedWriter;
  }

  public View attach(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new InvalidInputException("multipart request expected.");
    }
    List<FileItem> items = getFileItems(request);
    try {
      if (items.size() > 3) {
        throw new InvalidInputException("One file at a time.");
      }
      String attachmentName = null;
      Long baseRevision = null;
      FileItem file = null;
      for (FileItem item : items) {
        if (PARAM_ATTACHMENT_NAME.equals(item.getFieldName())) {
          attachmentName = item.getString().trim();
        }
        if (PARAM_BASE_REVISION.equals(item.getFieldName())) {
          baseRevision = RequestParameterReaders.getLong(item.getString().trim(), PARAM_BASE_REVISION);
        }
        if (!item.isFormField()) {
          file = item;
        }
      }
      if (baseRevision == null) {
        baseRevision = PageInfo.UNCOMMITTED;
      }

      if (file == null || file.getSize() == 0) {
        request.setAttribute("flash", ERROR_NO_FILE);
        return attachments(page, path, request, response);
      }
      else {
        InputStream in = file.getInputStream();
        try {
          // IE sends the full file path.
          storeAttachment(page, attachmentName, baseRevision, FilenameUtils.getName(file.getName()), in);
          return new RedirectToPageView(_wikiUrls, page, "/attachments/");
        }
        finally {
          IOUtils.closeQuietly(in);
        }
      }
    }
    finally {
      for (FileItem item : items) {
        item.delete();
      }
    }
  }

  @SuppressWarnings("unchecked")
  // commons-upload...
  private List<FileItem> getFileItems(final HttpServletRequest request) throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    return items;
  }

  private void storeAttachment(final PageReference page, final String attachmentName, final long baseRevision, final String fileName, final InputStream in) throws PageStoreException {
    String storeName = attachmentName;
    if (storeName == null || storeName.length() == 0) {
      storeName = fileName;
    }
    else if (storeName.indexOf('.') == -1) {
      storeName += fileName.substring(fileName.indexOf('.'));
    }
    String operation = baseRevision < 0 ? "Added" : "Updated";
    _store.attach(page, storeName, baseRevision, in, operation + " attachment " + attachmentName);
  }

  public View attachment(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String attachmentName = path.next();
    return new View() {
      public void render(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException, NotFoundException, PageStoreException, InvalidInputException {
        _store.attachment(page, attachmentName, getRevision(request), new ContentTypedSink() {
          public void setContentType(final String contentType) {
            response.setContentType(contentType);
          }

          public void setFileName(final String name) {
            response.setHeader("Content-Disposition", "attachment: filename=" + attachmentName);
          }

          public OutputStream stream() throws IOException {
            return response.getOutputStream();
          }
        });
      }
    };
  }

  public View attachments(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    request.setAttribute("attachments", _store.attachments(page));
    return new JspView("Attachments");
  }

  private boolean isLockTokenValid(final PageInfo pageInfo, final HttpServletRequest request, final boolean preview) throws InvalidInputException {
    final String username = (String) request.getAttribute(RequestAttributes.USERNAME);
    return pageInfo.lockedByUserIfNeeded(username) && (!preview || pageInfo.isNew() || lockTokenMatches(pageInfo, request));
  }

  private boolean lockTokenMatches(final PageInfo pageInfo, final HttpServletRequest request) throws InvalidInputException {
    return pageInfo.getLockToken().equals(getRequiredString(request, PARAM_LOCK_TOKEN));
  }

  private boolean isSessionIdValid(final HttpServletRequest request) {
    final String postedSessionId = request.getParameter(PARAM_SESSION_ID);
    final String requestedSessionId = request.getRequestedSessionId();
    return requestedSessionId != null && postedSessionId != null && postedSessionId.equals(requestedSessionId) && request.isRequestedSessionIdValid();
  }

  public View editor(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final boolean preview = request.getParameter(SUBMIT_PREVIEW) != null;
    PageInfo pageInfo = _store.getUnderlying().tryToLock(page);
    request.setAttribute(ATTR_PAGE_INFO, pageInfo);
    copySessionIdAsAttribute(request);
    if (!isLockTokenValid(pageInfo, request, preview)) {
      if (preview) {
        return diffEditorView(page, null, request);
      }
      else {
        request.setAttribute("flash", "Could not lock the page.");
        return new JspView("ViewPage");
      }
    }
    else {
      if (preview) {
        if (!isSessionIdValid(request)) {
          return diffEditorView(page, ERROR_SESSION_EXPIRED, request);
        }
        else {
          pageInfo = pageInfo.withAlternativeContent(getContent(request));
          request.setAttribute(ATTR_PAGE_INFO, pageInfo);
          ResultNode rendered = _renderer.render(pageInfo, pageInfo.getContent(), new ResponseSessionURLOutputFilter(response));
          request.setAttribute(ATTR_PREVIEW, rendered.toXHTML());
        }
      }
      return new JspView("EditPage");
    }
  }

  private void copySessionIdAsAttribute(final HttpServletRequest request) {
    request.setAttribute(ATTR_SESSION_ID, request.getSession().getId());
  }

  public View get(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final String revisionParam = request.getParameter(PARAM_REVISION);
    final String diffParam = request.getParameter(PARAM_DIFF_REVISION);
    addBacklinksInformation(request, page);
    final PageInfo main = pageInfoFromRevisionParam(page, revisionParam, PARAM_REVISION);
    request.setAttribute(ATTR_PAGE_INFO, main);
    request.setAttribute(ATTR_SHOW_REV, revisionParam != null);
    if (diffParam != null) {
      request.setAttribute(ATTR_DIFF_TITLE, page.getPath());
      final PageInfo compare = pageInfoFromRevisionParam(page, diffParam, PARAM_DIFF_REVISION);
      request.setAttribute(ATTR_DIFF_END_REV, revisionText(main.getRevision()));
      request.setAttribute(ATTR_DIFF_START_REV, revisionText(compare.getRevision()));
      request.setAttribute(ATTR_MARKED_UP_DIFF, _diffGenerator.getDiffMarkup(compare.getContent(), main.getContent()));
      if (compare.getRevision() > main.getRevision() && main.getRevision() > -1) {
        request.setAttribute("flash", "Note this diff is reversed.");
      }
      return new JspView("ViewDiff");
    }
    else if (ViewTypeConstants.is(request, ViewTypeConstants.CTYPE_RAW)) {
      return new RawPageView(main);
    }
    else {
      ResultNode rendered = _renderer.render(main, main.getContent(), new ResponseSessionURLOutputFilter(response));
      request.setAttribute(ATTR_RENDERED_CONTENTS, rendered.toXHTML());
      return new JspView("ViewPage");
    }
  }

  private String revisionText(final long revision) {
    if (revision == -1) {
      return "latest";
    }
    else if (revision == -2) {
      return "uncommitted";
    }
    else if (revision == -3) {
      return "deleted";
    }
    else {
      return "r" + revision;
    }
  }

  private void addBacklinksInformation(final HttpServletRequest request, final PageReference page) throws IOException, QuerySyntaxException, PageStoreException {
    List<String> pageNames = new ArrayList<String>(_graph.incomingLinks(page.getPath()));
    Collections.sort(pageNames);
    if (pageNames.size() > MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY) {
      pageNames = pageNames.subList(0, MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY);
      request.setAttribute(ATTR_BACKLINKS_LIMITED, true);
    }
    request.setAttribute(ATTR_BACKLINKS, pageNames);
  }

  public View history(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    List<ChangeInfo> changes = _store.history(page);
    if (ViewTypeConstants.is(request, CTYPE_ATOM)) {
      final String feedUrl = _wikiUrls.page(page.getName(), URLOutputFilter.NULL) + "?history&ctype=atom";
      return new FeedView(_configuration, _feedWriter, changes, feedUrl);
    }
    request.setAttribute("changes", changes);
    return new JspView("History");
  }

  public View set(final PageReference page, final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final boolean hasSaveParam = request.getParameter(SUBMIT_SAVE) != null;
    final boolean hasCopyParam = request.getParameter(SUBMIT_COPY) != null;
    final boolean hasRenameParam = request.getParameter(SUBMIT_RENAME) != null;
    final boolean hasUnlockParam = request.getParameter(SUBMIT_UNLOCK) != null;
    if (!(hasSaveParam ^ hasCopyParam ^ hasRenameParam ^ hasUnlockParam)) {
      throw new InvalidInputException("Exactly one action must be specified.");
    }

    if (hasSaveParam) {
      copySessionIdAsAttribute(request);
      if (!isSessionIdValid(request)) {
        return diffEditorView(page, ERROR_SESSION_EXPIRED, request);
      }
      String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
      String content = getContent(request);
      try {
        _store.set(page, lockToken, getBaseRevision(request), content, createLinkingCommitMessage(page, request));
      }
      catch (LostLockException e) {
        return diffEditorView(page, null, request);
      }
      catch (ConflictException e) {
        return diffEditorView(page, "Page has been updated since you started. Please do not save unless you feel that you can merge the changes.", request);
      }
    }
    else if (hasCopyParam) {
      final String fromPage = getString(request, PARAM_FROM_PAGE);
      final String toPage = getString(request, PARAM_TO_PAGE);
      if (!(fromPage == null ^ toPage == null)) {
        throw new InvalidInputException("'copy' requires one of toPage, fromPage");
      }
      final PageReference toRef;
      final PageReference fromRef;
      if (fromPage != null) {
        fromRef = new PageReferenceImpl(fromPage);
        toRef = page;
      }
      else {
        fromRef = page;
        toRef = new PageReferenceImpl(toPage);
      }
      _store.copy(fromRef, -1, toRef, createLinkingCommitMessage(toRef, request));
      return new RedirectToPageView(_wikiUrls, toRef);
    }
    else if (hasRenameParam) {
      final PageReference toPage = new PageReferenceImpl(getRequiredString(request, PARAM_TO_PAGE));
      _store.rename(page, toPage, -1, createLinkingCommitMessage(toPage, request));
      return new RedirectToPageView(_wikiUrls, toPage);
    }
    else if (hasUnlockParam) {
      String lockToken = request.getParameter(PARAM_LOCK_TOKEN);
      // New pages don't have a lock.
      if (lockToken != null && lockToken.length() > 0) {
        _store.unlock(page, lockToken);
      }
    }
    else {
      throw new InvalidInputException("No action specified.");
    }
    return new RedirectToPageView(_wikiUrls, page);
  }

  private View diffEditorView(final PageReference page, final String customMessage, final HttpServletRequest request) throws PageStoreException, InvalidInputException {
    PageInfo pageInfo = _store.getUnderlying().tryToLock(page);
    String message;
    final String content = getContent(request);
    final String newContent = pageInfo.getContent();
    pageInfo = pageInfo.withAlternativeContent(content);
    request.setAttribute(ATTR_MARKED_UP_DIFF, _diffGenerator.getDiffMarkup(newContent, content));
    if (pageInfo.isLocked() && !pageInfo.lockedByUserIfNeeded((String) request.getAttribute(RequestAttributes.USERNAME))) {
      message = "Page was locked by " + pageInfo.getLockedBy() + " on " + pageInfo.getLockedSince() + ".";
      pageInfo = pageInfo.withoutLockToken();
    }
    else {
      message = "Lock was lost!";
    }
    if (customMessage != null) {
      message = customMessage;
    }
    request.setAttribute(ATTR_PAGE_INFO, pageInfo);
    request.setAttribute("flash", message);
    return new JspView("EditPage");
  }

  private String getContent(final HttpServletRequest request) throws InvalidInputException {
    String content = getRequiredString(request, PARAM_CONTENT);
    content = content + Strings.CRLF;
    return content;
  }

  private Long getBaseRevision(final HttpServletRequest request) throws InvalidInputException {
    return getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
  }

  String createLinkingCommitMessage(final PageReference page, final HttpServletRequest request) {
    boolean minorEdit = request.getParameter(PARAM_MINOR_EDIT) != null;
    String commitMessage = request.getParameter(PARAM_COMMIT_MESSAGE);
    if (commitMessage == null || commitMessage.trim().length() == 0) {
      commitMessage = ChangeInfo.NO_COMMENT_MESSAGE_TAG;
    }
    return (minorEdit ? ChangeInfo.MINOR_EDIT_MESSAGE_TAG : "") + commitMessage + "\n" + _wikiUrls.page(page.getName(), URLOutputFilter.NULL);
  }

  private PageInfo pageInfoFromRevisionParam(final PageReference defaultPage, final String revisionText, final String paramName) throws InvalidInputException, PageStoreException {
    final PageRevisionReference reference = getPageRevisionReference(defaultPage, revisionText, paramName);
    return _store.get(reference.getPage(), reference.getRevision());
  }

  /**
   * Gets the PageInfo from a diff or rev parameter. I.e. "1234" or "PageName.1234"
   * @param defaultPage The page (used when the parameter does not override the page).
   * @param revisionText The parameter value.
   * @param paramName The name of the parameter for error messages.
   * @return The PageInfo.
   * @throws InvalidInputException When the parameter value is not of the correct form.
   * @throws PageStoreException
   */
  PageRevisionReference getPageRevisionReference(final PageReference defaultPage, final String revisionText, final String paramName) throws InvalidInputException {
    if (revisionText == null) {
      return new PageRevisionReference(defaultPage, -1);
    }

    final int dotIndex = revisionText.lastIndexOf('.');
    if (dotIndex != -1) {
      final Long revision = getLong(revisionText.substring(dotIndex + 1), paramName);
      final String pageName = revisionText.substring(0, dotIndex);
      final PageReference pageReference = pageName.length() > 0 ? new PageReferenceImpl(pageName) : defaultPage;
      return new PageRevisionReference(pageReference, revision);
    }
    return new PageRevisionReference(defaultPage, getLong(revisionText, paramName));
  }

  private static long getRevision(final HttpServletRequest request) throws InvalidInputException {
    Long givenRevision = getLong(request.getParameter(PARAM_REVISION), PARAM_REVISION);
    return givenRevision == null ? -1 : givenRevision;
  }

}
