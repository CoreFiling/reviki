package net.hillsdon.svnwiki.wiki.renderer;


public class LiteralResultNode extends LeafResultNode {

  private final String _xhtml;

  public LiteralResultNode(final String xhtml) {
    _xhtml = xhtml;
  }

  public String toXHTML() {
    return _xhtml;
  }

}
