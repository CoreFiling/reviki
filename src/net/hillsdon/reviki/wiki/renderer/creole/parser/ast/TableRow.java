package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.List;

public class TableRow extends ASTNode {
  public TableRow(List<ASTNode> cells) {
    super("tr", null, cells);
  }
}
