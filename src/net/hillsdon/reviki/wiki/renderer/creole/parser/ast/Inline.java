package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Inline extends ASTNode {
  public Inline(List<ASTNode> chunks) {
    super("", null, chunks);
  }

  public String toXHTML() {
    String out = "";

    for (ResultNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }
}
