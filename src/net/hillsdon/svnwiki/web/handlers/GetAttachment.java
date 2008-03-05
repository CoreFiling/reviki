package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getRevision;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.ContentTypedSink;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.ConsumedPath;
import net.hillsdon.svnwiki.web.InvalidInputException;

import org.apache.commons.fileupload.FileUploadException;

public class GetAttachment extends PageRequestHandler {

  public GetAttachment(final PageStore store) {
    super(store);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws InvalidInputException, FileUploadException, IOException, PageStoreException {
    final String attachmentName = path.next();
    getStore().attachment(page, attachmentName, getRevision(request), new ContentTypedSink() {
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

}
