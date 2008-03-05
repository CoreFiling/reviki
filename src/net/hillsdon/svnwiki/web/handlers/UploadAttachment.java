package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.RequestParameterReaders;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

public class UploadAttachment implements PageRequestHandler {

  private static final String PARAM_ATTACHMENT_NAME = "attachmentName";
  private static final String PARAM_BASE_REVISION = "baseRevision";
  private final PageStore _store;

  public UploadAttachment(final PageStore store) {
    _store = store;
  }

  @SuppressWarnings("unchecked")
  public void handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws InvalidInputException, FileUploadException, IOException, PageStoreException {
    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new InvalidInputException("multipart request expected.");
    }
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
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
      if (file == null) {
        throw new InvalidInputException("No file received.");
      }
      if (baseRevision == null) {
        baseRevision = PageInfo.UNCOMMITTED;
      }
      
      InputStream in = file.getInputStream();
      try {
        store(page, attachmentName, baseRevision, file.getName(), in);
      }
      finally {
        IOUtils.closeQuietly(in);
      }
    }
    finally {
      for (FileItem item : items) {
        item.delete();
      }
    }
    
    response.sendRedirect(request.getRequestURL().toString());
  }

  private void store(final PageReference page, final String attachmentName, final long baseRevision, final String fileName, final InputStream in) throws PageStoreException {
    String storeName = attachmentName;
    if (storeName == null || storeName.length() == 0) {
      storeName = fileName;
    }
    else if (storeName.indexOf('.') == -1) {
      storeName += fileName.substring(fileName.indexOf('.'));
    }
    String operation = baseRevision == PageInfo.UNCOMMITTED ? "Added" : "Updated";
    _store.attach(page, storeName, baseRevision, in, operation + " attachment " + attachmentName);
  }

}
