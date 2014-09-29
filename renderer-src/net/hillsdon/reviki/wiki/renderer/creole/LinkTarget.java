package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URISyntaxException;

import net.hillsdon.reviki.web.urls.UnknownWikiException;

public interface LinkTarget {

  boolean isNoFollow(LinkResolutionContext linkResolutionContext);

  String getStyleClass(LinkResolutionContext linkResolutionContext);

  String getURL(LinkResolutionContext linkResolutionContext) throws URISyntaxException, UnknownWikiException;

  boolean exists(LinkResolutionContext linkResolutionContext);

}
