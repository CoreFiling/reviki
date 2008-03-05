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

import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getRevision;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.NotFoundException;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.View;

import org.apache.commons.fileupload.FileUploadException;

public class GetAttachment implements PageRequestHandler {

  private final PageStore _store;

  public GetAttachment(final PageStore store) {
    _store = store;
  }

  @SuppressWarnings("unchecked")
  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws InvalidInputException, FileUploadException, IOException, PageStoreException {
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

}
