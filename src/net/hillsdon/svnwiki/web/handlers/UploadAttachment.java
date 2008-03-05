package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.InvalidInputException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

public class UploadAttachment extends PageRequestHandler {

  private static final String PARAM_ATTACHMENT_NAME = "attachmentName";

  public UploadAttachment(final PageStore store) {
    super(store);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final String page) throws InvalidInputException, FileUploadException, IOException {
    if (!ServletFileUpload.isMultipartContent(request)) {
      throw new InvalidInputException("multipart request expected.");
    }
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> items = upload.parseRequest(request);
    try {
      if (items.size() > 2) {
        throw new InvalidInputException("One file at a time.");
      }
      String attachmentName = null;
      FileItem file = null;
      for (FileItem item : items) {
        if (PARAM_ATTACHMENT_NAME.equals(item.getFieldName())) {
          attachmentName = item.getString().trim();
        }
        if (!item.isFormField()) {
          file = item;
        }
      }
      if (attachmentName == null || attachmentName.length() == 0) {
        throw new InvalidInputException(PARAM_ATTACHMENT_NAME + " too short.");
      }
      if (file == null) {
        throw new InvalidInputException("No file received.");
      }
      
      InputStream in = file.getInputStream();
      try {
        store(page, attachmentName, in);
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
    
    response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/pages/" + page));
  }

  private void store(final String page, final String attachmentName, final InputStream in) {
    System.err.println("Got attachment " + attachmentName + " for page " + page);
  }

}
