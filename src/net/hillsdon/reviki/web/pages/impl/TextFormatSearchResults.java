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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.web.common.View;

/**
 * Currently used by the JavaScript to display the autocomplete search box.
 * 
 * Perhaps JSON would be better.
 * 
 * @author mth
 */
public class TextFormatSearchResults implements View {
  
  private final Iterable<SearchMatch> _results;

  public TextFormatSearchResults(final Iterable<SearchMatch> results) {
    _results = results;
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();
    for (SearchMatch matcher : _results) {
      writer.println(matcher.getPage());
    }
  }
  
}