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