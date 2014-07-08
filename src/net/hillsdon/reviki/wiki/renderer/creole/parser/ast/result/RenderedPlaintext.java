package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import net.hillsdon.reviki.wiki.renderer.result.LeafResultNode;

public class RenderedPlaintext extends LeafResultNode {

  protected String contents;
  
  public RenderedPlaintext(String contents) {
    this.contents = contents;
  }

  public String toXHTML() {
    // TODO Escape entities
    return contents;
  }

}
