package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import net.hillsdon.reviki.wiki.renderer.result.LeafResultNode;

public class Raw extends LeafResultNode {

  protected String contents;
  
  public Raw(String contents) {
    this.contents = contents;
  }

  public String toXHTML() {
    return contents;
  }
}
