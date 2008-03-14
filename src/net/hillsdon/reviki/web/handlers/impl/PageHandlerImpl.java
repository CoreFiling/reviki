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
package net.hillsdon.reviki.web.handlers.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.AllPages;
import net.hillsdon.reviki.web.handlers.Attachments;
import net.hillsdon.reviki.web.handlers.FindPage;
import net.hillsdon.reviki.web.handlers.OrphanedPages;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.handlers.RecentChanges;
import net.hillsdon.reviki.web.handlers.RegularPage;

/**
 * Everything that does something to a wiki page or attachment comes through here.
 * 
 * @author mth
 */
public class PageHandlerImpl implements PageHandler {

  public static final String PATH_WALK_ERROR_MESSAGE = "No '/' characters allowed in a page name.";
  
  private final RegularPage _regularPage;
  private final FindPage _findPage;
  private final Attachments _attachments;

  private final RecentChanges _recentChanges;
  private final AllPages _allPages;
  private final OrphanedPages _orphanedPages;

  public PageHandlerImpl(final RecentChanges recentChanges, final AllPages allPages, final Attachments attachments, final RegularPage regularPage, final FindPage findPage, final OrphanedPages orphanedPages) {
    _recentChanges = recentChanges;
    _allPages = allPages;
    _attachments = attachments;
    _regularPage = regularPage;
    _findPage = findPage;
    _orphanedPages = orphanedPages;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String pageName = path.next();
    if (pageName == null || "".equals(pageName)) {
      return new RedirectView(RequestBasedWikiUrls.get(request).page("FrontPage"));
    }
    if (pageName.contains("/")) {
      throw new InvalidInputException(PATH_WALK_ERROR_MESSAGE);
    }
    PageReference page = new PageReference(pageName);
    request.setAttribute("page", page);

    if ("attachments".equals(path.peek())) {
      path.next();
      return _attachments.handlePage(path, request, response, page);
    }
    else if ("RecentChanges".equals(pageName)) {
      return _recentChanges.handlePage(path, request, response, page);
    }
    else if ("AllPages".equals(pageName)) {
      return _allPages.handlePage(path, request, response, page);
    }
    else if ("FindPage".equals(pageName)) {
      return _findPage.handlePage(path, request, response, page);
    }
    else if ("OrphanedPages".equals(pageName)) {
      return _orphanedPages.handle(path, request, response);
    }
    else {
      return _regularPage.handlePage(path, request, response, page);
    }
  }

}
