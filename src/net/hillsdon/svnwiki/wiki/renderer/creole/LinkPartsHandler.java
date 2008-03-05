package net.hillsdon.svnwiki.wiki.renderer.creole;

import net.hillsdon.svnwiki.vc.PageReference;

public interface LinkPartsHandler {

  String handle(PageReference page, RenderNode renderer, LinkParts parts);
  
}
