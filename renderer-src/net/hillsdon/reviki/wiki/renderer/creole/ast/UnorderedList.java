package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class UnorderedList extends ASTNode {
  public UnorderedList(final List<ASTNode> children) {
    super(children);

    _isBlock = true;
    _canContainBlock = true;
  }
}
