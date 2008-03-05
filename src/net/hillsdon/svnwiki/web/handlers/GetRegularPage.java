package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.external.diff_match_patch.diff_match_patch;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.InvalidInputException;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetRegularPage extends PageRequestHandler {

  private final MarkupRenderer _markupRenderer;

  @SuppressWarnings("unchecked") // Diff library is odd...
  private static String getDiffMarkup(PageInfo head, PageInfo base) {
    diff_match_patch api = new diff_match_patch();
    // Diff isn't public!
    LinkedList diffs = api.diff_main(base.getContent(), head.getContent());
    api.diff_cleanupSemantic(diffs);
    return api.diff_prettyHtml(diffs);
  }

  public GetRegularPage(final PageStore pageStore, final MarkupRenderer markupRenderer) {
    super(pageStore);
    _markupRenderer = markupRenderer;
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException, InvalidInputException {
    PageInfo head = store.get(page, -1);
    String diffRevision = request.getParameter("diff");
    request.setAttribute("pageInfo", head);

    if (diffRevision != null) {
      long baseRevision;
      try {
        baseRevision = Long.parseLong(diffRevision);
      }
      catch (NumberFormatException ex) {
        throw new InvalidInputException("'diff' must be a revision number.");
      }
      PageInfo base = store.get(page, baseRevision);
      request.setAttribute("markedUpDiff", getDiffMarkup(head, base));
      request.getRequestDispatcher("/WEB-INF/templates/ViewDiff.jsp").forward(request, response);
    }
    else {
      StringWriter writer = new StringWriter();
      _markupRenderer.render(head.getContent(), writer);
      
      request.setAttribute("renderedContents", writer.toString());
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").forward(request, response);
    }
  }

}
