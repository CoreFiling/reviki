package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Table extends ASTNode {
  public Table(List<ASTNode> rows) {
    super("table", null, rows);
  }
}
