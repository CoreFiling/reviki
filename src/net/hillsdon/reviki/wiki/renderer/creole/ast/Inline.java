package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Inline extends ASTNode {
  public Inline(List<ASTNode> chunks) {
    super("", null, chunks);
  }

  public String toXHTML() {
    String out = "";

    for (ASTNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }
}
