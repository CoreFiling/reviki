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

import net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.JspView;
import net.hillsdon.svnwiki.web.common.View;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.graph.WikiGraph;

public class RegularPage implements PageRequestHandler {

  private final PageRequestHandler _view;
  private final PageRequestHandler _editor;
  private final PageRequestHandler _set;
  private final PageRequestHandler _history;

  public RegularPage(final ConfigPageCachingPageStore cachingPageStore, final MarkupRenderer markupRenderer, final WikiGraph wikiGraph) {
    _view = new GetRegularPage(cachingPageStore, markupRenderer, wikiGraph);
    // It is important to use the non-caching page store here.  It is ok to view 
    // something out of date but users must edit the latest revision or else they
    // won't be able to commit.
    _editor = new EditorForPage(cachingPageStore.getUnderlying(), markupRenderer);
    _set = new SetPage(cachingPageStore);
    _history = new History(cachingPageStore);
  }
  
  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if (request.getParameter("history") != null) {
      return _history.handlePage(path, request, response, page);
    }
    else if ("POST".equals(request.getMethod())) {
      if (request.getParameter(SetPage.SUBMIT_SAVE) != null 
       || request.getParameter(SetPage.SUBMIT_COPY) != null
       || request.getParameter(SetPage.SUBMIT_RENAME) != null
       || request.getParameter(SetPage.SUBMIT_UNLOCK) != null) {
        return _set.handlePage(path, request, response, page);
      }
      else {
        return _editor.handlePage(path, request, response, page);
      }
    }
    else {
      if (request.getParameter(SetPage.SUBMIT_RENAME) != null) {
        return new JspView("Rename");
      }
      else if (request.getParameter(SetPage.SUBMIT_COPY) != null) {
        return new JspView("Copy");
      }
      return _view.handlePage(path, request, response, page);
    }
  }

}
