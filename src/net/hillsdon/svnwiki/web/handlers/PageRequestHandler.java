package net.hillsdon.svnwiki.web.handlers;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.RequestHandler;

/**
 * Common super-class to parse the page name out of the URL.
 * 
 * @author mth
 */
public abstract class PageRequestHandler  implements RequestHandler {

  private static final Pattern PAGE_FROM_URL = Pattern.compile("/pages/(.+?)(?:\\z|[/?])");
  private PageStore _store;

  public PageRequestHandler(final PageStore store) {
    _store = store;
  }
  
  public final void handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    Matcher matcher = PAGE_FROM_URL.matcher(request.getRequestURI());
    matcher.find();
    
    PageReference page = new PageReference(URLDecoder.decode(matcher.group(1), "UTF-8"));
    request.setAttribute("page", page);
    handlePage(request, response, page.getPath());
  }

  public PageStore getStore() {
    return _store;
  }
  
  public abstract void handlePage(HttpServletRequest request, HttpServletResponse response, String page) throws Exception;

}
