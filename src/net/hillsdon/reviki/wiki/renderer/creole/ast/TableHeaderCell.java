package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class TableHeaderCell extends TaggedNode {
  public TableHeaderCell(final List<ASTNode> contents) {
    super("th", contents);
  }
}
