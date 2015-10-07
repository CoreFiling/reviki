package net.hillsdon.reviki.vc.impl;

import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.PageStoreConfiguration;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.PageLinkTarget;

public class SVNPathLinkTarget extends PageLinkTarget {
  private final String _repositoryURL;
  private final String _path;

  public SVNPathLinkTarget(final String repositoryURL, final String path) {
    _repositoryURL = repositoryURL;
    _path = path;
  }

  @Override
  public boolean isLinkToCurrentWiki() {
    return false;
  }

  @Override
  protected String getWiki(LinkResolutionContext resolver) throws UnknownWikiException {
    return getWiki(resolver.getConfiguration());
  }

  private String getWiki(Configuration config) throws UnknownWikiException {
    if (config instanceof PageStoreConfiguration) {
      String url = _repositoryURL + _path;
      if (_repositoryURL.endsWith("/") && _path.startsWith("/")) {
        url = _repositoryURL + _path.substring(1);
      }
      PageStoreConfiguration configuration = (PageStoreConfiguration) config;
      for (WikiUrls wiki: configuration.getApplicationUrls().getAvailableWikiUrls()) {
        String wikiUrl = wiki.getWiki().getUrl().toString();
        if (!wikiUrl.endsWith("/")) {
          wikiUrl = wikiUrl + "/";
        }
        if (url.startsWith(wikiUrl)) {
          return wiki.getWikiName();
        }
      }
      throw new UnknownWikiException();
    }
    return null;
  }

  @Override
  public String getPageName() {
    return new PageReferenceImpl(_path).getName();
  }

  @Override
  protected String getRevision() {
    return null;
  }

  @Override
  protected String getFragment() {
    return null;
  }

}
