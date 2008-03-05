package net.hillsdon.svnwiki.web;

import javax.servlet.http.HttpServletRequest;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.wiki.WikiUrls;

public class RequestBasedWikiUrls implements WikiUrls {

  private String _base;

  public RequestBasedWikiUrls(final HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    _base = requestURL.substring(0, requestURL.length() - path.length());
  }
  
  public RequestBasedWikiUrls(final String base) {
    _base = base;
  }
  
  public boolean isPage(final String url) {
    return url.startsWith(_base + "/pages/") && !url.contains("/attachments/");
  }

  public String page(final String name) {
    return _base + "/pages/" + Escape.url(name);
  }

  public String root() {
    return _base + "/";
  }

  public String search() {
    return _base + "/pages/FindPage";
  }

  public String attachment(final String page, final String name) {
    return _base + Escape.url(page) + "/" + Escape.url(name);
  }

  public String feed() {
    return _base + "/pages/RecentChanges/atom.xml";
  }

  public boolean isAttachment(final String url) {
    return url.startsWith(_base + "/pages/") && url.contains("/attachments/") && !url.endsWith("/attachments/");
  }

  public boolean isAttachmentsDir(final String url) {
    return url.startsWith(_base + "/pages/") && url.endsWith("/attachments/");
  }

}
