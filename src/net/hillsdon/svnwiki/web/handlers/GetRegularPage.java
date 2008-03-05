package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getLong;

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

  private static final String PARAM_REVISION = "revision";
  private static final String PARAM_DIFF_REVISION = "diff";
  
  private final MarkupRenderer _markupRenderer;

  @SuppressWarnings("unchecked") // Diff library is odd...
  private static String getDiffMarkup(final PageInfo head, final PageInfo base) {
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

  private long getRevision(final HttpServletRequest request) throws InvalidInputException {
    Long givenRevision = getLong(request.getParameter(PARAM_REVISION), PARAM_REVISION);
    return givenRevision == null ? -1 : givenRevision;
  }
    
  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageStore store, final String page) throws PageStoreException, IOException, ServletException, InvalidInputException {
    long revison = getRevision(request);
    Long diffRevision = getLong(request.getParameter(PARAM_DIFF_REVISION), PARAM_DIFF_REVISION);

    PageInfo main = store.get(page, revison);
    request.setAttribute("pageInfo", main);
    if (diffRevision != null) {
      PageInfo base = store.get(page, diffRevision);
      request.setAttribute("markedUpDiff", getDiffMarkup(main, base));
      request.getRequestDispatcher("/WEB-INF/templates/ViewDiff.jsp").include(request, response);
    }
    else {
      StringWriter writer = new StringWriter();
      _markupRenderer.render(main.getContent(), writer);
      
      request.setAttribute("renderedContents", writer.toString());
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").include(request, response);
    }
  }

}
