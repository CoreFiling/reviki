package net.hillsdon.reviki.web.urls.impl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.reviki.web.urls.URLOutputFilter;

/**
 * Delegates to response.encodeURL.
 * 
 * @author pjt
 */
public class ResponseSessionURLOutputFilter implements URLOutputFilter {

  private final HttpServletResponse _response;

  private final HttpServletRequest _request;

  public ResponseSessionURLOutputFilter(final HttpServletRequest request, final HttpServletResponse response) {
    _request = request;
    _response = response;
  }

  public String filterURL(String url) {
    return shouldAppendSession(url) ? _response.encodeURL(url) : url;
  }

  /**
   * Get the effective port from a URL. If an explicit port is given in the url,
   * return that. Otherwise return port 443 for https:// scheme and 80
   * otherwise.
   * 
   * @param url
   * @return effective port number
   */
  private static int portFromUrl(final URL url) {
    // Get request port from URL
    int port = url.getPort();
    if (port != -1) {
      return port;
    }
    if ("https".equals(url.getProtocol())) {
      return 443;
    }
    return 80;
  }

  public boolean shouldAppendSession(final String urlStr) {
    // Reconstruct request URL
    URL requestUrl = null;
    try {
      requestUrl = new URL(_request.getRequestURL().toString());
    }
    catch (MalformedURLException e) {
      throw new AssertionError("Reconstructed request URL malformed: "+e.getMessage());
    }
    // Is this a valid absolute URL?
    URL url = null;
    try {
      url = new URL(urlStr);
    }
    catch (MalformedURLException e) {
      // Quick sanity check then assume a relative URL (which is safe)
      String urlLower = urlStr.toLowerCase();
      return !urlLower.startsWith("http:") && !urlLower.startsWith("https:");
    }

    // If protocol doesn't match (HTTP vs HTTPS)
    if (!requestUrl.getProtocol().equalsIgnoreCase(url.getProtocol())) {
      return false;
    }

    // If the host name doesn't match
    if (!requestUrl.getHost().equalsIgnoreCase(url.getHost())) {
      return false;
    }

    // If the port doesn't match
    if (portFromUrl(requestUrl) != portFromUrl(url)) {
      return false;
    }

    String contextPath = _request.getContextPath();
    
    String file = url.getFile();
    if (file == null) {
      file = "";
    }
    
    // If the contextPath doesn't match
    if (!file.startsWith(contextPath)) {
      return false;
    }
    
    // If all the above tests pass, we are ok to append the session
    return true;
  }

}
