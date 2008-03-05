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

/**
 * Everything that does something to a wiki page or attachment comes through here.
 * 
 * @author mth
 */
public class PageHandler implements RequestHandler {

  public static final String PATH_WALK_ERROR_MESSAGE = "No '/' characters allowed in a page name.";
  private final PageRequestHandler _regularPage;
  private final PageRequestHandler _attachments;

  private final RequestHandler _recentChanges;
  private final RequestHandler _allPages;
  private final RequestHandler _search;

  public PageHandler(final ConfigPageCachingPageStore cachingPageStore, final SearchEngine searchEngine, final MarkupRenderer markupRenderer) {
    _recentChanges = new RecentChanges(cachingPageStore);
    _allPages = new AllPages(cachingPageStore);
    _search = new Search(cachingPageStore, searchEngine);
    _attachments = new Attachments(cachingPageStore);
    _regularPage = new RegularPage(cachingPageStore, markupRenderer, searchEngine);
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
    
    if ("RecentChanges".equals(pageName)) {
      _recentChanges.handle(path, request, response);
    }
    else if ("AllPages".equals(pageName)) {
      _allPages.handle(path, request, response);
    }
    else if ("FindPage".equals(pageName)) {
      _search.handle(path, request, response);
    }
    else {
      if ("attachments".equals(path.peek())) {
        path.next();
        _attachments.handlePage(path, request, response, page);
      }
      else {
        _regularPage.handlePage(path, request, response, page);
      }
    }
  }

}
