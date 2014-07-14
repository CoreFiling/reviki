package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Page extends ASTNode {
  public Page(List<ASTNode> blocks) {
    super("", null, blocks);
  }

  public String toXHTML() {
    String out = "";

    for (ASTNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }
}
