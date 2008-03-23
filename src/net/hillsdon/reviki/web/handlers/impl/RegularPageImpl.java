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
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.EditorForPage;
import net.hillsdon.reviki.web.handlers.GetRegularPage;
import net.hillsdon.reviki.web.handlers.History;
import net.hillsdon.reviki.web.handlers.RegularPage;
import net.hillsdon.reviki.web.handlers.SetPage;

public class RegularPageImpl implements RegularPage {

  private final GetRegularPage _get;
  private final History _history;
  private final SetPage _set;
  private final EditorForPage _editor;

  public RegularPageImpl(final GetRegularPage get, final EditorForPage editor, final SetPage set, final History history) {
    _get = get;
    _editor = editor;
    _set = set;
    _history = history;
  }
  
  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    if (request.getParameter("history") != null) {
      return _history.handlePage(path, request, response, page);
    }
    else if ("POST".equals(request.getMethod())) {
      if (request.getParameter(SetPageImpl.SUBMIT_SAVE) != null 
       || request.getParameter(SetPageImpl.SUBMIT_COPY) != null
       || request.getParameter(SetPageImpl.SUBMIT_RENAME) != null
       || request.getParameter(SetPageImpl.SUBMIT_UNLOCK) != null) {
        return _set.handlePage(path, request, response, page);
      }
      else {
        return _editor.handlePage(path, request, response, page);
      }
    }
    else {
      if (request.getParameter(SetPageImpl.SUBMIT_RENAME) != null) {
        return new JspView("Rename");
      }
      else if (request.getParameter(SetPageImpl.SUBMIT_COPY) != null) {
        return new JspView("Copy");
      }
      return _get.handlePage(path, request, response, page);
    }
  }

}
