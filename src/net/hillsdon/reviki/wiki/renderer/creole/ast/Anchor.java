package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Anchor extends ASTNode {
  private final String _anchor;

  public Anchor(final String anchor) {
    _anchor = anchor;
  }

  /**
   * Get the name of this anchor.
   */
  public String getAnchor() {
    return _anchor;
  }
}
