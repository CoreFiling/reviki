package net.hillsdon.svnwiki.web;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.PageStoreFactory;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetRegularPage extends PageRequestHandler {

  private final MarkupRenderer _markupRenderer;

  public GetRegularPage(final PageStoreFactory pageStoreFactory, final MarkupRenderer markupRenderer) {
    super(pageStoreFactory);
    _markupRenderer = markupRenderer;
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException {
    PageInfo pageInfo = store.get(page);
    StringWriter writer = new StringWriter();
    _markupRenderer.render(pageInfo.getContent(), writer);
    
    request.setAttribute("renderedContents", writer.toString());
    request.setAttribute("pageInfo", pageInfo);
    request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").forward(request, response);
  }

}
