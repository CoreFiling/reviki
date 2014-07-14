package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.List;

public class OrderedList extends ASTNode {
  public OrderedList(List<ASTNode> children) {
    super("ol", children);
  }
}
