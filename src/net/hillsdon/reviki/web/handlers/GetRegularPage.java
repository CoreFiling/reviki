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

import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRevision;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.external.diff_match_patch.diff_match_patch;
import net.hillsdon.reviki.search.QuerySyntaxException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.graph.WikiGraph;

public class GetRegularPage implements PageRequestHandler {

  public static final int MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY = 15;

  public static final String PARAM_DIFF_REVISION = "diff";

  public static final String ATTR_PAGE_INFO = "pageInfo";
  public static final String ATTR_BACKLINKS = "backlinks";
  public static final String ATTR_BACKLINKS_LIMITED = "backlinksLimited";
  public static final String ATTR_RENDERED_CONTENTS = "renderedContents";
  public static final String ATTR_MARKED_UP_DIFF = "markedUpDiff";

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

  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws PageStoreException, IOException, ServletException, InvalidInputException, QuerySyntaxException {
    long revison = getRevision(request);
    Long diffRevision = getLong(request.getParameter(PARAM_DIFF_REVISION), PARAM_DIFF_REVISION);
    addBacklinksInformation(request, page);

    final PageInfo main = _store.get(page, revison);
    request.setAttribute(ATTR_PAGE_INFO, main);
    if (diffRevision != null) {
      PageInfo base = _store.get(page, diffRevision);
      request.setAttribute(ATTR_MARKED_UP_DIFF, getDiffMarkup(main, base));
      return new JspView("ViewDiff");
    }
    else if (request.getParameter("raw") != null) {
      return new RawPageView(main);
    }
    else {
      StringWriter writer = new StringWriter();
      _markupRenderer.render(main, main.getContent(), writer);
      request.setAttribute(ATTR_RENDERED_CONTENTS, writer.toString());
      return new JspView("ViewPage");
    }
  }

  private void addBacklinksInformation(final HttpServletRequest request, final PageReference page) throws IOException, QuerySyntaxException, PageStoreException {
    List<String> pageNames = new ArrayList<String>(_graph.incomingLinks(page.getPath()));
    Collections.sort(pageNames);
    if (pageNames.size() > MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY) {
      pageNames = pageNames.subList(0, MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY);
      request.setAttribute(ATTR_BACKLINKS_LIMITED, true);
    }
    request.setAttribute(ATTR_BACKLINKS, pageNames);
  }

}
