package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Inline extends ASTNode {
  public Inline(List<ASTNode> chunks) {
    super("", null, chunks);
  }

  public String toXHTML() {
    String out = "";

    boolean first = true;
    for (ASTNode node : getChildren()) {
      if (!first) {
        out += " ";
      }

      out += node.toXHTML();
      first = false;
    }

    return out;
  }
}
