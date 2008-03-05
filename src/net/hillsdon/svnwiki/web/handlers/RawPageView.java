package net.hillsdon.svnwiki.web.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.web.common.View;

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
