package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

public class Link extends LinkNode {
  public Link(final String target, final String title, final PageInfo page, final LinkPartsHandler handler) {
    super(target, title, page, handler);
  }
}
