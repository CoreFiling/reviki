package net.hillsdon.reviki.web.taglib;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import net.hillsdon.reviki.web.urls.ApplicationUrls;
import net.hillsdon.reviki.web.urls.ResourceUrls;
import net.hillsdon.reviki.web.urls.WikiUrls;

/**
 * Accessors for Reviki objects available to JSP tags.
 *
 * @author mth
 */
public class PageContextAccess {

  static ResourceUrls getBestResourceUrls(final PageContext pageContext) {
    final ServletRequest request = pageContext.getRequest();
    final ApplicationUrls application = (ApplicationUrls) request.getAttribute(ApplicationUrls.KEY);
    final WikiUrls wiki = (WikiUrls) request.getAttribute(WikiUrls.KEY);
    return wiki != null ? wiki : application;
  }

}
