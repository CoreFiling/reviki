package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Inline extends ASTNode {
  public Inline(final List<ASTNode> chunks) {
    super(chunks);
  }
  
  @Override
  protected void toSmallString(StringBuilder sb) {
    for (ASTNode child : getChildren()) {
      child.toSmallString(sb);
    }
  }
}
