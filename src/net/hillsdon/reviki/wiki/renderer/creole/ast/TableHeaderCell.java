package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableHeaderCell extends ASTNode {
  public TableHeaderCell(ASTNode body) {
    super("th", body);
  }
}
