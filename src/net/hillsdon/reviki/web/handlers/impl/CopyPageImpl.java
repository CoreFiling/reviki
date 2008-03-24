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

import static net.hillsdon.reviki.web.common.RequestParameterReaders.getLong;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRequiredString;
import static net.hillsdon.reviki.web.handlers.impl.SetPageImpl.PARAM_COMMIT_MESSAGE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.CopyPage;
import net.hillsdon.reviki.wiki.WikiUrls;

public class CopyPageImpl implements CopyPage {

  private static final String PARAM_FROM_PAGE = "fromPage";
  private static final String PARAM_FROM_REVISION = "fromRevision";
  private static final String PARAM_TO_PAGE = "toPage";
  
  private final PageStore _store;
  private final WikiUrls _urls;

  public CopyPageImpl(final CachingPageStore store, final WikiUrls urls) {
    _store = store;
    _urls = urls;
  }

  public View handlePage(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response, final PageReference page) throws Exception {
    final String fromPage = getRequiredString(request, PARAM_FROM_PAGE);
    final long fromRevision = getLong(PARAM_FROM_REVISION, getRequiredString(request, PARAM_FROM_REVISION));
    final String toPage = getRequiredString(request, PARAM_TO_PAGE);
    final String commitMessage = getRequiredString(request, PARAM_COMMIT_MESSAGE);
    _store.copy(new PageReference(fromPage), fromRevision, new PageReference(toPage), commitMessage);
    return new RedirectView(_urls.page(toPage));
  }

}
