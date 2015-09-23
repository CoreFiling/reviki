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
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import org.apache.commons.httpclient.util.URIUtil;

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
  
  private static String getUrlForPage(final HttpServletRequest request, final String wiki, final String page) throws Exception {
    LinkResolutionContext resolver = (LinkResolutionContext) request.getAttribute("linkResolutionContext");
    return resolver.resolve(wiki, page).toString();
  }

  public void render(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    String wiki;
    String page;
    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();
    for (SearchMatch matcher : _results) {
      wiki = matcher.getWiki();
      page = matcher.getPage();
      
      if (matcher.isSameWiki()) {
        writer.println(matcher.getPage());
      }
      else {
        StringBuilder s = new StringBuilder(wiki + ";" + page);
        try {
          s.append(";" + getUrlForPage(request, wiki, page));
        }
        catch (UnknownWikiException e) {
          // Do not respond with URL component
        }
        writer.println(s.toString());
      }
    }
    if (request.getHeader("Authorization") == null) {
      writer.println("Not logged in;Log in to see all results;" + getUrlForPage(request, null, "FindPage") + "?login=force&query=" + URIUtil.encodeWithinQuery(request.getParameter("query")));
    }
  }

}
