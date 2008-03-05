package net.hillsdon.svnwiki.wiki.renderer.creole;


/**
 * "{{"..."}}" for inline images.
 * 
 * @author mth
 */
public class CreoleImageNode extends LinkNode {

  public CreoleImageNode(final LinkPartsHandler handler) {
    // Disambiguate from {{{nowiki}}}.
    super("[{][{]([^{].*?)[}][}]", new CreoleLinkContentsSplitter(), handler);
  }
  
}
