package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.RequestHandler;

public class Search implements RequestHandler {

  private static final String PARAM_QUERY = "query";
  
  private final PageStore _store;
  private final SearchEngine _searchEngine;

  public Search(final PageStore store, final SearchEngine searchEngine) {
    _store = store;
    _searchEngine = searchEngine;
  }

  public void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String query = request.getParameter(PARAM_QUERY);
    if (request.getParameter("force") == null && _store.list().contains(new PageReference(query))) {
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/pages/" + query));
    }
    else {
      if (isWikiWord(query)) {
        request.setAttribute("suggestCreate", query);
      }
      request.setAttribute("results", _searchEngine.search(query));
      request.getRequestDispatcher("/WEB-INF/templates/SearchResults.jsp").include(request, response);
    }
  }

}
