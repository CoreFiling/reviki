package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Table extends TaggedNode {
  public Table(final List<ASTNode> rows) {
    super("table", rows);
  }
}
