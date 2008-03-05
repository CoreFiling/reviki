package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;

public class Attachments implements PageRequestHandler {

  private final PageRequestHandler _list;
  private final PageRequestHandler _upload;
  private final PageRequestHandler _get;

  public Attachments(final PageStore pageStore) {
    _list = new ListAttachments(pageStore);
    _upload = new UploadAttachment(pageStore);
    _get = new GetAttachment(pageStore);
  }

  public void handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if (path.hasNext()) {
      _get.handlePage(path, request, response, page);
    }
    else {
      if (request.getMethod().equals("POST")) {
        _upload.handlePage(path, request, response, page);
      }
      else {
        _list.handlePage(path, request, response, page);
      }
    }
  }

}
