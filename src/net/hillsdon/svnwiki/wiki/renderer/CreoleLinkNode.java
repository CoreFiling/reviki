package net.hillsdon.svnwiki.wiki.renderer;


/**
 * "[["..."]]" links.
 * 
 * @author mth
 */
public class CreoleLinkNode extends AbstractLinkNode {

  public CreoleLinkNode(final LinkPartHandler handler) {
    super("\\[\\[(.*?)\\]\\]", new CreoleLinkContentsSplitter(), handler);
  }
  
}
