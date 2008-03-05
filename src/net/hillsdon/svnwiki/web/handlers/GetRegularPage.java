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

import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.svnwiki.web.common.RequestParameterReaders.getRevision;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.external.diff_match_patch.diff_match_patch;
import net.hillsdon.svnwiki.search.QuerySyntaxException;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.WikiGraph;

public class GetRegularPage implements PageRequestHandler {

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
  private final WikiGraph _graph;
  private final PageStore _store;

  public GetRegularPage(final PageStore store, final MarkupRenderer markupRenderer, final WikiGraph graph) {
    _store = store;
    _markupRenderer = markupRenderer;
    _graph = graph;
  }

  public void handlePage(ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, InvalidInputException, QuerySyntaxException {
    long revison = getRevision(request);
    Long diffRevision = getLong(request.getParameter(PARAM_DIFF_REVISION), PARAM_DIFF_REVISION);
    addBacklinksInformation(request, page);

    PageInfo main = _store.get(page, revison);
    request.setAttribute("pageInfo", main);
    if (diffRevision != null) {
      PageInfo base = _store.get(page, diffRevision);
      request.setAttribute("markedUpDiff", getDiffMarkup(main, base));
      request.getRequestDispatcher("/WEB-INF/templates/ViewDiff.jsp").include(request, response);
    }
    else if (request.getParameter("raw") != null) {
      // This is a cludge.  We should represent 'special' pages better.
      if (page.getPath().equals("ConfigCss")) {
        response.setContentType("text/css");
      }
      else {
        response.setContentType("text/plain");
      }
      response.getWriter().write(main.getContent());
    }
    else {
      StringWriter writer = new StringWriter();
      _markupRenderer.render(main, main.getContent(), writer);
      
      request.setAttribute("renderedContents", writer.toString());
      request.getRequestDispatcher("/WEB-INF/templates/ViewPage.jsp").include(request, response);
    }
  }

  private void addBacklinksInformation(final HttpServletRequest request, final PageReference page) throws IOException, QuerySyntaxException, PageStoreException {
    List<String> pageNames = new ArrayList<String>(_graph.incomingLinks(page.getPath()));
    Collections.sort(pageNames);
    if (pageNames.size() > BACKLINKS_LIMIT) {
      pageNames = pageNames.subList(0, BACKLINKS_LIMIT - 1);
      request.setAttribute("backlinksLimited", true);
    }
    request.setAttribute("backlinks", pageNames);
  }

}
