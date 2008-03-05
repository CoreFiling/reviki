package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreFactory;

public class SetPage extends PageRequestHandler {

  public SetPage(final PageStoreFactory pageStoreFactory) {
    super(pageStoreFactory);
  }
  
  @Override
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws Exception {
    String baseRevisionString = request.getParameter("baseRevision");
    if (baseRevisionString == null) {
      throw new InvalidInputException("'baseRevision' required.");
    }
    long baseRevision;
    try {
      baseRevision = Long.parseLong(baseRevisionString);
    }
    catch (NumberFormatException ex) {
      throw new InvalidInputException("'baseRevision' invalid.");
    }
    
    String content = request.getParameter("content");
    if (content == null) {
      throw new InvalidInputException("'content' required.");
    }
    if (!content.endsWith("\n")) {
      content = content + "\n";
    }
    
    store.set(page, baseRevision, content);
  }

}
