package net.hillsdon.svnwiki.web.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.QuerySyntaxException;
import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.web.RequestHandler;

public class Search implements RequestHandler {

  private static final String PARAM_QUERY = "query";
  
  private final SearchEngine _searchEngine;

  public Search(final SearchEngine searchEngine) {
    _searchEngine = searchEngine;
  }

  public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, QuerySyntaxException {
    String query = request.getParameter(PARAM_QUERY);
    request.setAttribute("results", _searchEngine.search(query));
    request.getRequestDispatcher("/WEB-INF/templates/SearchResults.jsp").include(request, response);
  }

}
