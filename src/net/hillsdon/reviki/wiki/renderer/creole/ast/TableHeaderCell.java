package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableHeaderCell extends TaggedNode {
  public TableHeaderCell(final ASTNode inner) {
    super("th", inner);
  }
}
