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
import net.hillsdon.svnwiki.web.common.RedirectView;
import net.hillsdon.svnwiki.web.common.RequestParameterReaders;
import net.hillsdon.svnwiki.web.common.View;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

public class UploadAttachment implements PageRequestHandler {

  public static final String ERROR_NO_FILE = "Please browse to a non-empty file to upload.";
  
  private static final String PARAM_ATTACHMENT_NAME = "attachmentName";
  private static final String PARAM_BASE_REVISION = "baseRevision";
  private final PageStore _store;
  private final PageRequestHandler _listAttachments;

  public UploadAttachment(final PageStore store, final PageRequestHandler _list) {
    _store = store;
    _listAttachments = _list;
  }

  @SuppressWarnings("unchecked")
  public View handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
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
      if (baseRevision == null) {
        baseRevision = PageInfo.UNCOMMITTED;
      }
      
      if (file == null || file.getSize() == 0) {
        request.setAttribute("flash", ERROR_NO_FILE);
        return _listAttachments.handlePage(path, request, response, page);
      }
      else {
        InputStream in = file.getInputStream();
        try {
          store(page, attachmentName, baseRevision, file.getName(), in);
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

  private void store(final PageReference page, final String attachmentName, final long baseRevision, final String fileName, final InputStream in) throws PageStoreException {
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

}
