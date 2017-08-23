package net.hillsdon.reviki.converter;

import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleLinkHandler;

class JiraLinkHandler extends SimpleLinkHandler {

  public JiraLinkHandler(final String fmat, final LinkResolutionContext context) {
    super(fmat, context);
  }

  @Override
  public boolean isAcronymNotLink(final LinkParts parts) {
    return false;
  }

}