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
package net.hillsdon.reviki.web.dispatching.impl;

import static net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences.COMPLIMENTARY_CONTENT_PAGES;

import java.io.IOException;

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
import net.hillsdon.reviki.web.dispatching.ResourceHandler;
import net.hillsdon.reviki.web.dispatching.WikiHandler;
import net.hillsdon.reviki.web.handlers.PageHandler;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.vcintegration.BuiltInPageReferences;
import net.hillsdon.reviki.web.vcintegration.RequestLifecycleAwareManager;
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

  private final RequestLifecycleAwareManager _requestLifecycleAwareManager;
  private final SvnWikiRenderer _renderer;
  private final CachingPageStore _cachingPageStore;
  private final InternalLinker _internalLinker;
  private final ChangeNotificationDispatcher _syncUpdater;
  private final ResourceHandler _resources;
  private final PageHandler _handler;

  public WikiHandlerImpl(CachingPageStore cachingPageStore, SvnWikiRenderer renderer, InternalLinker internalLinker, ChangeNotificationDispatcher syncUpdater, RequestLifecycleAwareManager requestLifecycleAwareManager, ResourceHandler resources, PageHandler handler) {
    _cachingPageStore = cachingPageStore;
    _renderer = renderer;
    _internalLinker = internalLinker;
    _syncUpdater = syncUpdater;
    _requestLifecycleAwareManager = requestLifecycleAwareManager;
    _resources = resources;
    _handler = handler;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    try {
      _requestLifecycleAwareManager.requestStarted(request);
      try {
        if ("resources".equals(path.peek())) {
          return _resources.handle(path.consume(), request, response);
        }
        
        request.setAttribute("cssUrl", _internalLinker.url(BuiltInPageReferences.CONFIG_CSS.getPath()) + "?raw");
        request.setAttribute("internalLinker", _internalLinker);
        _syncUpdater.sync();
        addSideBarEtcToRequest(request);
        // We need to complete the rendering here, so the view can call back into the page store.
        _handler.handle(path, request, response).render(request, response);
        return View.NULL;
      }
      finally {
        _requestLifecycleAwareManager.requestComplete();
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
    for (PageReference ref : COMPLIMENTARY_CONTENT_PAGES) {
      final String requestVarName = "rendered" + ref.getPath().substring("Config".length());
      PageInfo page = _cachingPageStore.get(ref, -1);
      request.setAttribute(requestVarName, _renderer.render(ref, page.getContent()).toXHTML());
    }
  }

}
