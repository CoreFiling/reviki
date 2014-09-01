package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class ListItem extends ASTNode {
  public ListItem(final List<ASTNode> children) {
    super(children);

    _isBlock = true;
    _canContainBlock = true;
  }

  public ListItem(final ASTNode body) {
    super(body);

    _isBlock = true;
    _canContainBlock = true;
  }
}
