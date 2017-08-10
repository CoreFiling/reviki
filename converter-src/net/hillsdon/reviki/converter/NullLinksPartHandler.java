package net.hillsdon.reviki.converter;

import java.net.URISyntaxException;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;

class NullLinksPartHandler implements LinkPartsHandler {

  @Override
  public boolean isAcronymNotLink(final LinkParts parts) {
    return true;
  }

  @Override
  public String handle(final PageReference page, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    return null;
  }

  @Override
  public String handle(final PageReference page, final String xhtmlContent, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    return null;
  }

  @Override
  public LinkResolutionContext getContext() {
    return null;
  }

}