package net.hillsdon.reviki.web.urls;

import java.net.URI;
import java.net.URISyntaxException;

import net.hillsdon.reviki.web.urls.SimpleWikiUrls;

import com.google.common.base.Function;

/**
 * A simplification of WikiUrls providing only what the renderer needs.
 *
 * @author msw
 */
public interface SimpleWikiUrls {
  /** Construct a SimpleWikiUrls which has all URLs relative to a given path. */
  public static final Function<String, SimpleWikiUrls> RELATIVE_TO = new Function<String, SimpleWikiUrls>() {
    public SimpleWikiUrls apply(final String baseurl) {
      return new SimpleWikiUrls() {
        public String pagesRoot() {
          return baseurl;
        }

        public URI page(String pageName) {
          URI root = URI.create(pagesRoot());
          try {
            String path = root.getPath();
            if (!path.endsWith("/")) {
              path = path + "/";
            }
            return new URI(root.getScheme(), root.getUserInfo(), root.getHost(), root.getPort(), path + pageName, root.getQuery(), root.getFragment());
          }
          catch (URISyntaxException e) {
            throw new RuntimeException(e);
          }
        }
      };
    }
  };

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
