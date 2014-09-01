package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class OrderedList extends ASTNode {
  public OrderedList(final List<ASTNode> children) {
    super(children);

    _isBlock = true;
    _canContainBlock = true;
  }
}
