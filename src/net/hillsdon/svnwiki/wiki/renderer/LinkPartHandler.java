package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.vc.PageReference;

public interface LinkPartHandler {

  String handle(PageReference page, RenderNode renderer, LinkParts parts);
  
}
