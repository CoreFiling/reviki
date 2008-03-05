package net.hillsdon.svnwiki.wiki.renderer;


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
