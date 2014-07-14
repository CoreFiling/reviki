package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Page extends ASTNode {
  public Page(List<ASTNode> blocks) {
    super("", null, blocks);
  }

  public String toXHTML() {
    String out = "";

    for (ResultNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }
}
