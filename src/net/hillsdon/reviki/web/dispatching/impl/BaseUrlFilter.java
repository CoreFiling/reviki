package net.hillsdon.reviki.web.dispatching.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class BaseUrlFilter implements Filter {

  private static final String HEADER_BASE_URL = "X-Reviki-Base-Location";
  public static final String ATTR_BASE_URL = "baseURL";

  public static String getBaseUrl(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    String base = requestURL.substring(0, requestURL.length() - path.length());
    return base;
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String baseURL = httpRequest.getHeader(HEADER_BASE_URL);
      if (baseURL == null) {
        baseURL = getBaseUrl(httpRequest);
      }
      if (baseURL.endsWith("/")) {
        baseURL = baseURL.substring(0, baseURL.length() - 1);
      }
      request.setAttribute(ATTR_BASE_URL, baseURL);
    }
    chain.doFilter(request, response);
  }

  public void destroy() {
  }

  public void init(final FilterConfig config) throws ServletException {
  }

}
