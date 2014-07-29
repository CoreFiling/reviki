package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Page extends ASTNode {
  public Page(final List<ASTNode> blocks) {
    super(blocks);
  }
  
  @Override
  public String toSmallString() {
    String out = "";
    for (ASTNode child : getChildren()) {
      out += child.toSmallString();
    }
    return out;
  }
}
