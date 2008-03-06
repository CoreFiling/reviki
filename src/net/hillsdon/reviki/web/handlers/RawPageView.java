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
package net.hillsdon.reviki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.common.View;

/**
 * A raw view of a page.
 * 
 * Much improvement needed to avoid mime-type hack... 
 * 
 * @author mth
 */
public class RawPageView implements View {
  
  private final PageInfo _page;

  public RawPageView(final PageInfo page) {
    _page = page;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    // This is a cludge.  We should represent 'special' pages better.
    if (_page.getPath().equals("ConfigCss")) {
      response.setContentType("text/css");
    }
    else {
      response.setContentType("text/plain");
    }
    response.getWriter().write(_page.getContent());
  }
  
}
