/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.web.handlers;

import static java.lang.String.format;
import static net.hillsdon.reviki.text.WikiWordUtils.isWikiWord;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.View;

public class FindPage implements PageRequestHandler {

  private static final String OPENSEARCH_DESCRIPTION =
    "<?xml version='1.0' encoding='UTF-8'?>\n"
  + "<OpenSearchDescription xmlns='http://a9.com/-/spec/opensearch/1.1/'>\n"
  + "<ShortName>Wiki Search</ShortName>\n"
  + "<Description>Wiki Search</Description>\n"
  + "<Url type='text/html' template='%s?query={searchTerms}'/>\n"
  + "</OpenSearchDescription>\n";
  
  static final String PARAM_QUERY = "query";
  private static final String PARAM_QUERY_ALTERNATE = "q";

  
  private final PageStore _store;
  private final SearchEngine _searchEngine;
  private final PageRequestHandler _regularPage;

  public FindPage(final PageStore store, final SearchEngine searchEngine, final PageRequestHandler regularPage) {
    _store = store;
    _searchEngine = searchEngine;
    _regularPage = regularPage;
  }

  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if ("opensearch.xml".equals(path.next())) {
      return new View() {
        public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
          response.setContentType("application/opensearchdescription+xml");
          response.getWriter().write(format(OPENSEARCH_DESCRIPTION, Escape.html(RequestBasedWikiUrls.get(request).search())));
        }
      };
    }
    String query = request.getParameter(PARAM_QUERY);
    if (query == null) {
      request.getParameter(PARAM_QUERY_ALTERNATE);
    }
    if (query == null) {
      return _regularPage.handlePage(path, request, response, page);
    }
    
    boolean pageExists = _store.list().contains(new PageReference(query));
    if (request.getParameter("force") == null && pageExists) {
      return new RedirectView(RequestBasedWikiUrls.get(request).page(query));
    }
    
    final Set<SearchMatch> results = _searchEngine.search(query, true);
    Long limit = getLong(request.getParameter("limit"), "limit");
    if (limit != null) {
      results.retainAll(new ArrayList<SearchMatch>(results).subList(0, (int) Math.min(results.size(), limit)));
    }
    if ("txt".equals(request.getParameter("ctype"))) {
      return new View() {
        public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
          response.setContentType("text/plain");
          PrintWriter writer = response.getWriter();
          for (SearchMatch matcher : results) {
            writer.println(matcher.getPage());
          }
        }
      };
    }
    else {
      if (!pageExists && isWikiWord(query)) {
        request.setAttribute("suggestCreate", query);
      }
      request.setAttribute("results", results);
      return new JspView("SearchResults");
    }
  }

}
