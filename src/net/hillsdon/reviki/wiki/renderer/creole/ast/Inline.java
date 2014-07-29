package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Inline extends ASTNode {
  public Inline(final List<ASTNode> chunks) {
    super(chunks);
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
