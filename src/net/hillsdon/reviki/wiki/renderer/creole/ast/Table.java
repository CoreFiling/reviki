package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Table extends TaggedNode {
  public static final String TABLE_ALIGNMENT_DIRECTIVE = "table-alignment";

  public Table(final List<ASTNode> rows) {
    super("table", rows);
  }
}
