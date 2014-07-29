package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class TableRow extends ASTNode {
  public TableRow(final List<ASTNode> cells) {
    super(cells);
  }
}
