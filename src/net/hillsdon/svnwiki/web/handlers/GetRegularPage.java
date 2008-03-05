package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.web.handlers.RequestParameterReaders.getLong;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.external.diff_match_patch.diff_match_patch;
import net.hillsdon.svnwiki.search.QuerySyntaxException;
import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.search.SearchMatch;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.InvalidInputException;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class GetRegularPage extends PageRequestHandler {

  private static final String PARAM_DIFF_REVISION = "diff";
  private static final int BACKLINKS_LIMIT = 15;

  @SuppressWarnings("unchecked") // Diff library is odd...
  private static String getDiffMarkup(final PageInfo head, final PageInfo base) {
    diff_match_patch api = new diff_match_patch();
    // Diff isn't public!
    LinkedList diffs = api.diff_main(base.getContent(), head.getContent());
    api.diff_cleanupSemantic(diffs);
    return api.diff_prettyHtml(diffs);
  }
  
  private final MarkupRenderer _markupRenderer;
  private final SearchEngine _engine;

  public GetRegularPage(final PageStore pageStore, final MarkupRenderer markupRenderer, final SearchEngine engine) {
    super(pageStore);
    _markupRenderer = markupRenderer;
    _engine = engine;
  }

  public void handlePage(final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, InvalidInputException, QuerySyntaxException {
    long revison = RequestParameterReaders.getRevision(request);
    Long diffRevision = getLong(request.getParameter(PARAM_DIFF_REVISION), PARAM_DIFF_REVISION);
    addBacklinksInformation(request, page);

    PageInfo main = getStore().get(page, revison);
    request.setAttribute("pageInfo", main);
    if (diffRevision != null) {
      PageInfo base = getStore().get(page, diffRevision);
      request.setAttribute("markedUpDiff", getDiffMarkup(main, base));
      request.getRequestDispatcher("/WEB-INF/templates/ViewDiff.jsp").include(request, response);
    }
    else {
      StringWriter writer = new StringWriter();
      _markupRenderer.render(main, main.getContent(), writer);
      
      request.setAttribute("renderedContents", writer.toString());
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").include(request, response);
    }
  }

  private void addBacklinksInformation(final HttpServletRequest request, final PageReference page) throws IOException, QuerySyntaxException, PageStoreException {
    Set<SearchMatch> backlinks = _engine.search(page.getPath());
    backlinks.remove(new SearchMatch(page.getPath(), null));
    if (backlinks.size() > BACKLINKS_LIMIT) {
      request.setAttribute("backlinksLimited", true);
    }
    
    Set<String> pageNames = new LinkedHashSet<String>();
    int i = 0;
    for (SearchMatch backlink : backlinks) {
      pageNames.add(backlink.getPage());
      if (++i >= BACKLINKS_LIMIT) {
        break;
      }
    }
    request.setAttribute("backlinks", pageNames);
  }

}
