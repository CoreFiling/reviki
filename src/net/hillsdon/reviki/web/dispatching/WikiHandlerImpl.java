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
package net.hillsdon.reviki.web.dispatching;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.ChangeNotificationDispatcher;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalBasicSVNOperations;
import net.hillsdon.reviki.web.vcintegration.RequestScopedThreadLocalPageStore;
import net.hillsdon.reviki.web.vcintegration.SpecialPagePopulatingPageStore;
import net.hillsdon.reviki.wiki.InternalLinker;
import net.hillsdon.reviki.wiki.renderer.SvnWikiRenderer;

/**
 * A particular wiki (sub-wiki, whatever).
 * 
 * @author mth
 */
public class WikiHandlerImpl implements WikiHandler {

  private static final class RequestAuthenticationView implements View {
    public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
      response.setHeader("WWW-Authenticate", "Basic realm=\"Wiki login\"");
      response.sendError(401);
    }
  }

  public static final String ATTRIBUTE_WIKI_IS_VALID = "wikiIsValid";

  private final RequestScopedThreadLocalPageStore _pageStore;
  private final SvnWikiRenderer _renderer;
  private final CachingPageStore _cachingPageStore;
  private final InternalLinker _internalLinker;
  private final ChangeNotificationDispatcher _syncUpdater;
  private final RequestScopedThreadLocalBasicSVNOperations _operations;
  private final PageHandler _handler;

  public WikiHandlerImpl(RequestScopedThreadLocalPageStore pageStore, CachingPageStore cachingPageStore, SvnWikiRenderer renderer, InternalLinker internalLinker, ChangeNotificationDispatcher syncUpdater, RequestScopedThreadLocalBasicSVNOperations operations, PageHandler handler) {
    _pageStore = pageStore;
    _cachingPageStore = cachingPageStore;
    _renderer = renderer;
    _internalLinker = internalLinker;
    _syncUpdater = syncUpdater;
    _operations = operations;
    _handler = handler;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    request.setAttribute("cssUrl", _internalLinker.url("ConfigCss") + "?raw");
    request.setAttribute("internalLinker", _internalLinker);
    try {
      // Handle the lifecycle of the thread-local stuff.
      // Should have common interfaces and a new class...
      _operations.create(request);
      _pageStore.create(request);
      try {
        _syncUpdater.sync();
        addSideBarEtcToRequest(request);
        // We need to complete the rendering here, so the view can call back into the page store.
        _handler.handle(path, request, response).render(request, response);
        return View.NULL;
      }
      finally {
        _operations.destroy();
        _pageStore.destroy();
      }
    }
    catch (PageStoreAuthenticationException ex) {
      return new RequestAuthenticationView();
    }
    catch (Exception ex) {
      // Rather horrible, needed at the moment for auth failures during rendering (linking).
      if (ex.getCause() instanceof PageStoreAuthenticationException) {
        return new RequestAuthenticationView();
      }
      else {
        // Don't try to show wiki header/footer.
        request.setAttribute(ATTRIBUTE_WIKI_IS_VALID, false);
        throw ex;
      }
    }
  }

  private void addSideBarEtcToRequest(final HttpServletRequest request) throws PageStoreException, IOException {
    for (PageReference ref : SpecialPagePopulatingPageStore.COMPLIMENTARY_CONTENT_PAGES) {
      final String requestVarName = "rendered" + ref.getPath().substring("Config".length());
      StringWriter html = new StringWriter();
      PageInfo page = _cachingPageStore.get(ref, -1);
      _renderer.render(ref, page.getContent(), html);
      request.setAttribute(requestVarName, html.toString());
    }
  }

}
