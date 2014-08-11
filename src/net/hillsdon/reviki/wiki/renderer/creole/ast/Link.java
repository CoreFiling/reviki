package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

public class Link extends LinkNode {
  public Link(final String target, final String title, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    super("a", target, title, page, urlOutputFilter, handler);
  }
}
