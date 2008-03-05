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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.InvalidInputException;
import net.hillsdon.svnwiki.web.common.RequestHandler;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.WikiGraph;
import net.hillsdon.svnwiki.wiki.WikiGraphImpl;

/**
 * Everything that does something to a wiki page or attachment comes through here.
 * 
 * @author mth
 */
public class PageHandler implements RequestHandler {

  public static final String PATH_WALK_ERROR_MESSAGE = "No '/' characters allowed in a page name.";
  private final PageRequestHandler _regularPage;
  private final PageRequestHandler _findPage;
  private final PageRequestHandler _attachments;

  private final RequestHandler _recentChanges;
  private final RequestHandler _allPages;
  private final RequestHandler _orphanedPages;

  public PageHandler(final ConfigPageCachingPageStore cachingPageStore, final SearchEngine searchEngine, final MarkupRenderer markupRenderer) {
    WikiGraph wikiGraph = new WikiGraphImpl(cachingPageStore, searchEngine);
    _recentChanges = new RecentChanges(cachingPageStore);
    _allPages = new AllPages(cachingPageStore);
    _attachments = new Attachments(cachingPageStore);
    _regularPage = new RegularPage(cachingPageStore, markupRenderer, wikiGraph);
    _findPage = new FindPage(cachingPageStore, searchEngine, _regularPage);
    _orphanedPages = new OrphanedPages(wikiGraph);
  }

  public void handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String pageName = path.next();
    if (pageName == null || "".equals(pageName)) {
      response.sendRedirect(request.getContextPath() + "/pages/" + request.getAttribute("wikiName") + "/FrontPage");
      return;
    }
    if (pageName.contains("/")) {
      throw new InvalidInputException(String.format(PATH_WALK_ERROR_MESSAGE, pageName));
    }
    PageReference page = new PageReference(pageName);
    request.setAttribute("page", page);

    if ("attachments".equals(path.peek())) {
      path.next();
      _attachments.handlePage(path, request, response, page);
    }
    else if ("RecentChanges".equals(pageName)) {
      _recentChanges.handle(path, request, response);
    }
    else if ("AllPages".equals(pageName)) {
      _allPages.handle(path, request, response);
    }
    else if ("FindPage".equals(pageName)) {
      _findPage.handlePage(path, request, response, page);
    }
    else if ("OrphanedPages".equals(pageName)) {
      _orphanedPages.handle(path, request, response);
    }
    else {
      _regularPage.handlePage(path, request, response, page);
    }
  }

}
