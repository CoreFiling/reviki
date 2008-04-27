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

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.configuration.ApplicationUrls;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.InvalidInputException;
import net.hillsdon.reviki.web.common.RedirectView;
import net.hillsdon.reviki.web.common.View;
import net.hillsdon.reviki.web.handlers.JumpToWikiUrl;
import static net.hillsdon.reviki.web.common.RequestParameterReaders.getRequiredString;

public class JumpToWikiUrlImpl implements JumpToWikiUrl {

  private ApplicationUrls _urls;

  public JumpToWikiUrlImpl(final ApplicationUrls urls) {
    _urls = urls;
  }
  
  public View handle(final ConsumedPath path, final HttpServletRequest request, final HttpServletResponse response) throws PageStoreException, IOException, ServletException, InvalidInputException {
    final String wiki = URLEncoder.encode(getRequiredString(request, "name"), "UTF-8");
    return new RedirectView(_urls.get(wiki).page("FrontPage"));
  }

}
