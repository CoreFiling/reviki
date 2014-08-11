package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class OrderedList extends TaggedNode {
  public OrderedList(final List<ASTNode> children) {
    super("ol", children);
  }
}
