package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class TableCell extends TaggedNode {
  public TableCell(final List<ASTNode> children) {
    super("td", children);

    _isBlock = true;
    _canContainBlock = true;
  }
}
