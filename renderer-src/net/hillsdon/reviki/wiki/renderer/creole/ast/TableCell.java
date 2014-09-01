package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class TableCell extends ASTNode {
  public TableCell(final List<ASTNode> inner) {
    super(inner);

    _isBlock = true;
    _canContainBlock = true;
  }
}