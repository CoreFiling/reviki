package net.hillsdon.reviki.configuration;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.reviki.web.common.WikiUrlsImpl;
import net.hillsdon.reviki.wiki.WikiUrls;

public class ApplicationUrlsImpl implements ApplicationUrls {

  private static String getBaseUrl(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String base = requestURL.substring(0, requestURL.length() - path.length());
    return base;
  }

  private final String _base;

  public ApplicationUrlsImpl(final HttpServletRequest request) {
    this(getBaseUrl(request));
  }
  
  public ApplicationUrlsImpl(final String base) {
    _base = base;
  }

  public WikiUrls get(final String name) {
    return new WikiUrlsImpl(this, name);
  }

  public String list() {
    return url("/list");
  }

  public String url(String relative) {
    return _base + relative;
  }

}
