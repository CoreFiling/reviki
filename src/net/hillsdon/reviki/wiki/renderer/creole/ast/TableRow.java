package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class TableRow extends TaggedNode {
  public TableRow(final List<ASTNode> cells) {
    super("tr", cells);
  }
}
