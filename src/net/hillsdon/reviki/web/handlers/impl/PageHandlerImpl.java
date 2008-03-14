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
import net.hillsdon.reviki.web.handlers.Attachments;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.handlers.RegularPage;
import net.hillsdon.reviki.web.handlers.SpecialPage;

/**
 * Everything that does something to a wiki page or attachment comes through here.
 * 
 * @author mth
 */
public class PageHandlerImpl implements PageHandler {

  public static final String PATH_WALK_ERROR_MESSAGE = "No '/' characters allowed in a page name.";
  
  private final RegularPage _regularPage;
  private final Attachments _attachments;
  private final SpecialPagesImpl _specialPages;

  public PageHandlerImpl(final RegularPage regularPage, final Attachments attachments, final SpecialPagesImpl specialPages) {
    _attachments = attachments;
    _regularPage = regularPage;
    _specialPages = specialPages;
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

    final SpecialPage specialPage = _specialPages.get(pageName);
    // Even special pages can have attachments.
    if ("attachments".equals(path.peek())) {
      path.next();
      return _attachments.handlePage(path, request, response, page);
    }
    else if (specialPage != null) {
      return specialPage.handlePage(path, request, response, page);
    }
    else {
      return _regularPage.handlePage(path, request, response, page);
    }
  }

}
