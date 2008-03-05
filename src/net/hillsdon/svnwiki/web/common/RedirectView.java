package net.hillsdon.svnwiki.web.common;

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
