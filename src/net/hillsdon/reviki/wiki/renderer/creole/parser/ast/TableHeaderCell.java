package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

public class TableHeaderCell extends ASTNode {
  public TableHeaderCell(ASTNode body) {
    super("th", body);
  }
}
