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

import net.hillsdon.reviki.configuration.WikiConfiguration;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;

public class FeedView implements View {

  private final FeedWriter _feedWriter;
  private final List<ChangeInfo> _changes;
  private final String _feedUrl;
  private final WikiConfiguration _wikiConfiguration;

  public FeedView(final WikiConfiguration wikiConfiguration, final FeedWriter feedWriter, final List<ChangeInfo> changes, final String feedUrl) {
    _wikiConfiguration = wikiConfiguration;
    _feedWriter = feedWriter;
    _changes = changes;
    _feedUrl = feedUrl;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/atom+xml");
    final String title = _wikiConfiguration.getWikiName() + " wiki";
    _feedWriter.writeAtom(title, _feedUrl, _changes, response.getOutputStream());
  }

}