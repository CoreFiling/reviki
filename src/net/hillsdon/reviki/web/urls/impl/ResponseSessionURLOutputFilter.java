package net.hillsdon.reviki.web.urls.impl;

import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.web.urls.URLOutputFilter;

/**
 * Delegates to response.encodeURL.
 * 
 * @author pjt
 */
public class ResponseSessionURLOutputFilter implements URLOutputFilter {

  private final HttpServletResponse _response;

  public ResponseSessionURLOutputFilter(final HttpServletResponse response) {
    _response = response;
  }

  public String filterURL(String url) {
    return _response.encodeURL(url);
  }

}
