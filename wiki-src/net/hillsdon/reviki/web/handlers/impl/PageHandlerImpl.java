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

import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_COPY;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_RENAME;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_SAVE;
import static net.hillsdon.reviki.web.pages.impl.DefaultPageImpl.SUBMIT_UNLOCK;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.COMPLIMENTARY_CONTENT_PAGES;
import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.PAGE_FRONT_PAGE;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.pages.Page;
import net.hillsdon.reviki.web.pages.PageSource;
import net.hillsdon.reviki.web.pages.impl.DefaultPageImpl;
import net.hillsdon.reviki.web.redirect.RedirectToPageView;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.SvnWikiRenderer;

/**
 * Everything that does something to a wiki page or attachment comes through here
 * and is dispatched to the relevant method on the appropriate page implementation.
 *
 * @author mth
 */
public class PageHandlerImpl implements PageHandler {

  public static final String PATH_WALK_ERROR_MESSAGE = "No '/' characters allowed in a page name.";

  private final CachingPageStore _cachingPageStore;
  private final PageSource _pageSource;
  private final MarkupRenderer<String> _renderer;
  private final WikiUrls _wikiUrls;

  public PageHandlerImpl(final PageSource pageSource, final WikiUrls wikiUrls, final SvnWikiRenderer renderer, CachingPageStore cachingPageStore) {
    _cachingPageStore = cachingPageStore;
    _pageSource = pageSource;
    _renderer = renderer;
    _wikiUrls = wikiUrls;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String pageName = path.next();
    if (pageName == null || "".equals(pageName)) {
      return new RedirectToPageView(_wikiUrls, PAGE_FRONT_PAGE);
    }
    if (pageName.contains("/")) {
      throw new InvalidInputException(PATH_WALK_ERROR_MESSAGE);
    }

    final PageReference pageReference = new PageReferenceImpl(pageName);
    final boolean isPost = request.getMethod().equals("POST");
    request.setAttribute("page", pageReference);

    final Page page = _pageSource.get(pageReference);
    if ("attachments".equals(path.peek())) {
      path.next();
      if (path.hasNext()) {
        if (isPost && request.getParameter(DefaultPageImpl.SUBMIT_DELETE) != null) {
          return page.deleteAttachment(pageReference, path, request, response);
        }
        return page.attachment(pageReference, path, request, response);
      }
      else {
        if (isPost) {
          return page.attach(pageReference, path, request, response);
        }
        else {
          addSideBarEtcToRequest(request, response);
          return page.attachments(pageReference, path, request, response);
        }
      }
    }
    else if(path.hasNext() && !"".equals(path.peek()) && !"opensearch.xml".equals(path.peek())) {
      throw new NotFoundException();
    }
    else if (request.getParameter("history") != null) {
      if (!ViewTypeConstants.is(request, ViewTypeConstants.CTYPE_ATOM)) {
        addSideBarEtcToRequest(request, response);
      }
      return page.history(pageReference, path, request, response);
    }
    else if (isPost) {
      if (request.getParameter(SUBMIT_SAVE) != null
       || request.getParameter(SUBMIT_COPY) != null
       || request.getParameter(SUBMIT_RENAME) != null
       || request.getParameter(SUBMIT_UNLOCK) != null) {
        return page.set(pageReference, path, request, response);
      }
      else {
        addSideBarEtcToRequest(request, response);
        return page.editor(pageReference, path, request, response);
      }
    }
    else {
      if (request.getParameter(SUBMIT_RENAME) != null) {
        addSideBarEtcToRequest(request, response);
        return new JspView("Rename");
      }
      else if (request.getParameter(SUBMIT_COPY) != null) {
        addSideBarEtcToRequest(request, response);
        return new JspView("Copy");
      }
      else {
        if (!ViewTypeConstants.is(request, ViewTypeConstants.CTYPE_RAW)) {
          addSideBarEtcToRequest(request, response);
        }
        return page.get(pageReference, path, request, response);
      }
    }
  }

  private void addSideBarEtcToRequest(final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException {
    for (PageReference ref : COMPLIMENTARY_CONTENT_PAGES) {
      final String requestVarName = "rendered" + ref.getPath().substring("Config".length());
      VersionedPageInfo page = _cachingPageStore.get(ref, -1);
      request.setAttribute(requestVarName, _renderer.render(page, new ResponseSessionURLOutputFilter(request, response)).get());
    }
  }
}
