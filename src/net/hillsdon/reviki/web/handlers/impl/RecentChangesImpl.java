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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.RequestBasedWikiUrls;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.RecentChanges;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;

public class RecentChangesImpl implements RecentChanges {

  /**
   * We don't actually do 'recent' in terms of date as that's less useful.
   */
  private static final int RECENT_CHANGES_HISTORY_SIZE = 50;

  private final PageStore _store;

  public RecentChangesImpl(final PageStore store) {
    _store = store;
  }

  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final List<ChangeInfo> recentChanges = getRecentChanges(request.getParameter("showMinor") != null);
    if ("atom.xml".equals(path.next())) {
      return new View() {
        public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
          response.setContentType("application/atom+xml");
          FeedWriter.writeAtom(RequestBasedWikiUrls.get(request), response.getWriter(), recentChanges);
        }
      };
    }
    request.setAttribute("recentChanges", recentChanges);
    return new JspView("RecentChanges");
  }

  private List<ChangeInfo> getRecentChanges(final boolean showMinor) throws PageStoreException {
    List<ChangeInfo> allChanges = _store.recentChanges(RecentChangesImpl.RECENT_CHANGES_HISTORY_SIZE);
    if (showMinor) {
      return allChanges;
    }
    
    List<ChangeInfo> majorChanges = new ArrayList<ChangeInfo>();
    for (ChangeInfo change : allChanges) {
      if (!change.isMinorEdit()) {
        majorChanges.add(change);
      }
    }
    return majorChanges;
  }

}
