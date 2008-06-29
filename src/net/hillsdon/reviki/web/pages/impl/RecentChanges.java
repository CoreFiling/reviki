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
package net.hillsdon.reviki.web.pages.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.fij.core.Predicate;
import net.hillsdon.fij.core.Predicates;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RequestParameterReaders;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;

import static net.hillsdon.fij.core.Functional.filter;
import static net.hillsdon.fij.core.Functional.list;

import static net.hillsdon.reviki.web.common.ViewTypeConstants.CTYPE_ATOM;

public class RecentChanges extends AbstractSpecialPage {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  static final long RECENT_CHANGES_DEFAULT_HISTORY_SIZE = 50;
  static final long RECENT_CHANGES_MAX_HISTORY_SIZE = 250;
  
  private static final Predicate<ChangeInfo> MAJOR_ONLY = new Predicate<ChangeInfo>() {
    public Boolean transform(final ChangeInfo in) {
      return !in.isMinorEdit();
    }
  };

  private final PageStore _store;
  private final FeedWriter _feedWriter;
  private final WikiUrls _wikiUrls;

  public RecentChanges(final DefaultPage defaultPage, final CachingPageStore store, final FeedWriter feedWriter, final WikiUrls wikiUrls) {
    super(defaultPage);
    _store = store;
    _feedWriter = feedWriter;
    _wikiUrls = wikiUrls;
  }

  @Override
  public View get(PageReference page, ConsumedPath path, HttpServletRequest request, HttpServletResponse response) throws Exception {
    final boolean showMinor = request.getParameter("showMinor") != null;
    final List<ChangeInfo> recentChanges = getRecentChanges(getLimit(request), showMinor);
    request.setAttribute("recentChanges", recentChanges);
    if (ViewTypeConstants.is(request, CTYPE_ATOM)) {
      return new FeedView(_feedWriter, recentChanges, _wikiUrls.feed());
    }
    return new JspView("RecentChanges");
  }

  private Long getLimit(HttpServletRequest request) throws InvalidInputException {
    Long limit = RequestParameterReaders.getLong(request, "limit");
    if (limit == null) {
      limit = RECENT_CHANGES_DEFAULT_HISTORY_SIZE;
    }
    limit = Math.min(limit, RECENT_CHANGES_MAX_HISTORY_SIZE);
    return limit;
  }

  private List<ChangeInfo> getRecentChanges(final long limit, final boolean showMinor) throws PageStoreException {
    return list(filter(_store.recentChanges(limit), showMinor ? Predicates.<ChangeInfo>all() : MAJOR_ONLY));
  }

  public String getName() {
    return "RecentChanges";
  }

}
