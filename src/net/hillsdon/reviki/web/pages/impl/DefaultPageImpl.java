package net.hillsdon.reviki.web.pages.impl;

import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRequiredString;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRevision;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.external.diff_match_patch.diff_match_patch;
import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.RequestParameterReaders;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.RawPageView;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

public class DefaultPageImpl implements DefaultPage {

  public static final String SUBMIT_SAVE = "save";
  public static final String SUBMIT_COPY = "copy";
  public static final String SUBMIT_RENAME = "rename";
  public static final String SUBMIT_UNLOCK = "unlock";
  public static final String SUBMIT_PREVIEW = "preview";

  public static final String PARAM_TO_PAGE = "toPage";
  public static final String PARAM_FROM_PAGE = "fromPage";
  public static final String PARAM_CONTENT = "content";
  public static final String PARAM_BASE_REVISION = "baseRevision";
  public static final String PARAM_LOCK_TOKEN = "lockToken";
  public static final String PARAM_COMMIT_MESSAGE = "description";
  public static final String PARAM_MINOR_EDIT = "minorEdit";
  public static final String PARAM_DIFF_REVISION = "diff";
  public static final String PARAM_ATTACHMENT_NAME = "attachmentName";

  public static final String ATTR_PAGE_INFO = "pageInfo";
  public static final String ATTR_BACKLINKS = "backlinks";
  public static final String ATTR_BACKLINKS_LIMITED = "backlinksLimited";
  public static final String ATTR_RENDERED_CONTENTS = "renderedContents";
  public static final String ATTR_MARKED_UP_DIFF = "markedUpDiff";
  
  public static final String ERROR_NO_FILE = "Please browse to a non-empty file to upload.";

  public static final int MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY = 15;

  static String createLinkingCommitMessage(final HttpServletRequest request) {
    boolean minorEdit = request.getParameter(PARAM_MINOR_EDIT) != null;
    String commitMessage = request.getParameter(PARAM_COMMIT_MESSAGE);
    if (commitMessage == null || commitMessage.trim().length() == 0) {
      commitMessage = ChangeInfo.NO_COMMENT_MESSAGE_TAG;
    }
    return (minorEdit ? ChangeInfo.MINOR_EDIT_MESSAGE_TAG : "") + commitMessage + "\n" + request.getRequestURL();
  }
  
  private final CachingPageStore _store;
  private final MarkupRenderer _renderer;
  private final WikiGraph _graph;
  
  public DefaultPageImpl(final CachingPageStore store, final MarkupRenderer renderer, final WikiGraph graph) {
    _store = store;
    _renderer = renderer;
    _graph = graph;
  }
  
  public View attach(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
          storeAttachment(page, attachmentName, baseRevision, file.getName(), in);
          return new RedirectView(request.getRequestURL().toString());
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

  @SuppressWarnings("unchecked") // commons-upload...
  private List<FileItem> getFileItems(HttpServletRequest request) throws FileUploadException {
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


  public View attachment(final PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
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

  public View attachments(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.setAttribute("attachments", _store.attachments(page));
    return new JspView("Attachments");
  }

  public View editor(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    PageInfo pageInfo = _store.getUnderlying().tryToLock(page);
    request.setAttribute("pageInfo", pageInfo);
    if (!pageInfo.lockedByUserIfNeeded((String) request.getAttribute(RequestAttributes.USERNAME))) {
      request.setAttribute("flash", "Could not lock the page.");
      return new JspView("ViewPage");
    }
    else {
      if (request.getParameter(SUBMIT_PREVIEW) != null) {
        pageInfo = pageInfo.withAlternativeContent(getRequiredString(request, PARAM_CONTENT));
        request.setAttribute("pageInfo", pageInfo);
        ResultNode rendered = _renderer.render(pageInfo, pageInfo.getContent());
        request.setAttribute("preview", rendered.toXHTML());
      }
      return new JspView("EditPage");
    }
  }

  @SuppressWarnings("unchecked") // Diff library is odd...
  private static String getDiffMarkup(final PageInfo head, final PageInfo base) {
    diff_match_patch api = new diff_match_patch();
    // Diff isn't public!
    LinkedList diffs = api.diff_main(base.getContent(), head.getContent());
    api.diff_cleanupSemantic(diffs);
    return api.diff_prettyHtml(diffs);
  }
  
  public View get(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    long revison = getRevision(request);
    Long diffRevision = getLong(request.getParameter(PARAM_DIFF_REVISION), PARAM_DIFF_REVISION);
    addBacklinksInformation(request, page);

    final PageInfo main = _store.get(page, revison);
    request.setAttribute(ATTR_PAGE_INFO, main);
    if (diffRevision != null) {
      PageInfo base = _store.get(page, diffRevision);
      request.setAttribute(ATTR_MARKED_UP_DIFF, getDiffMarkup(main, base));
      return new JspView("ViewDiff");
    }
    else if (request.getParameter("raw") != null) {
      return new RawPageView(main);
    }
    else {
      ResultNode rendered = _renderer.render(main, main.getContent());
      request.setAttribute(ATTR_RENDERED_CONTENTS, rendered.toXHTML());
      return new JspView("ViewPage");
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

  public View history(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    List<ChangeInfo> changes = _store.history(page);
    request.setAttribute("changes", changes);
    return new JspView("History");
  }

  public View set(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    final boolean hasSaveParam = request.getParameter(SUBMIT_SAVE) != null;
    final boolean hasCopyParam = request.getParameter(SUBMIT_COPY) != null;
    final boolean hasRenameParam = request.getParameter(SUBMIT_RENAME) != null;
    final boolean hasUnlockParam = request.getParameter(SUBMIT_UNLOCK) != null;
    if (!(hasSaveParam ^ hasCopyParam ^ hasRenameParam ^ hasUnlockParam)) {
      throw new InvalidInputException("Exactly one action must be specified.");
    }
    
    if (hasSaveParam) {
      String lockToken = getRequiredString(request, PARAM_LOCK_TOKEN);
      String content = getRequiredString(request, PARAM_CONTENT);
      if (!content.endsWith(Strings.CRLF)) {
        content = content + Strings.CRLF;
      }
      _store.set(page, lockToken, getBaseRevision(request), content, createLinkingCommitMessage(request));
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
        fromRef = new PageReference(fromPage);
        toRef = page;
      }
      else {
        fromRef = page;
        toRef = new PageReference(toPage);
      }
      _store.copy(fromRef, -1, toRef, createLinkingCommitMessage(request));
      return new RedirectView(RequestBasedWikiUrls.get(request).page(toRef.getPath()));
    }
    else if (hasRenameParam) {
      final String toPage = getRequiredString(request, PARAM_TO_PAGE);
      _store.rename(page, new PageReference(toPage), -1, createLinkingCommitMessage(request));
      return new RedirectView(RequestBasedWikiUrls.get(request).page(toPage));
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
    return new RedirectView(request.getRequestURL().toString());
  }
  
  private Long getBaseRevision(final HttpServletRequest request) throws InvalidInputException {
    return getLong(getRequiredString(request, PARAM_BASE_REVISION), PARAM_BASE_REVISION);
  }

}
