package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class ListItem extends TaggedNode {
  public ListItem(final List<ASTNode> children) {
    super("li", children);

    _isBlock = true;
    _canContainBlock = true;
  }

  public ListItem(final ASTNode body) {
    super("li", body);

    _isBlock = true;
    _canContainBlock = true;
  }
}
