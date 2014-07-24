package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableHeaderCell extends ASTNode {
  public TableHeaderCell(final ASTNode body) {
    super("th", body);
  }
}
