package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkNode;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkPartsHandler;


/**
 * "[["..."]]" links.
 * 
 * @author mth
 */
public class CreoleLinkNode extends LinkNode {

  public CreoleLinkNode(final LinkPartsHandler handler) {
    super("\\[\\[(.*?)\\]\\]", new CreoleLinkContentsSplitter(), handler);
  }
  
}
