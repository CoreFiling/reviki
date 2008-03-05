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

import static java.lang.String.format;
import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.RequestBasedWikiUrls;

public class FindPage implements PageRequestHandler {

  private static final String OPENSEARCH_DESCRIPTION =
    "<?xml version='1.0' encoding='UTF-8'?>\n"
  + "<OpenSearchDescription xmlns='http://a9.com/-/spec/opensearch/1.1/'>\n"
  + "<ShortName>Wiki Search</ShortName>\n"
  + "<Description>Wiki Search</Description>\n"
  + "<Url type='text/html' template='%s?query={searchTerms}'/>\n"
  + "</OpenSearchDescription>\n";
  
  static final String PARAM_QUERY = "query";
  
  private final PageStore _store;
  private final SearchEngine _searchEngine;
  private final PageRequestHandler _regularPage;

  public FindPage(final PageStore store, final SearchEngine searchEngine, final PageRequestHandler regularPage) {
    _store = store;
    _searchEngine = searchEngine;
    _regularPage = regularPage;
  }

  public void handlePage(ConsumedPath path, HttpServletRequest request, HttpServletResponse response, PageReference page) throws Exception {
    if ("opensearch.xml".equals(path.next())) {
      response.setContentType("application/opensearchdescription+xml");
      response.getWriter().write(format(OPENSEARCH_DESCRIPTION, Escape.html(new RequestBasedWikiUrls(request).search())));
      return;
    }
    String query = request.getParameter(PARAM_QUERY);
    if (query == null) {
      _regularPage.handlePage(path, request, response, page);
      return;
    }
    
    boolean pageExists = _store.list().contains(new PageReference(query));
    if (request.getParameter("force") == null && pageExists) {
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/pages/" + request.getAttribute("wikiName") + "/" + query));
    }
    else {
      if (!pageExists && isWikiWord(query)) {
        request.setAttribute("suggestCreate", query);
      }
      request.setAttribute("results", _searchEngine.search(query, true));
      request.getRequestDispatcher("/WEB-INF/templates/SearchResults.jsp").include(request, response);
    }
  }

}
