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
package net.hillsdon.reviki.web.common;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Communicates a redirect.
 * 
 * @author mth
 */
public class RedirectView implements View {

  private final String _url;

  public RedirectView(final String url) {
    _url = url;
  }
  
  public String getURL() {
    return _url;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    response.sendRedirect(_url);
  }
  
}
