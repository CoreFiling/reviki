package net.hillsdon.reviki.web.urls;

import java.net.URI;

/**
 * A simplification of WikiUrls providing only what the renderer needs.
 *
 * @author msw
 */
public interface SimpleWikiUrls {
  /**
   * Note that if the the returned String is going to be used as a link, it must
   * be encoded with a {@link URLOutputFilter}.
   * 
   * @return
   */
  String pagesRoot();

  /**
   * Get the URI of a page.
   */
  URI page(String pageName);
}
