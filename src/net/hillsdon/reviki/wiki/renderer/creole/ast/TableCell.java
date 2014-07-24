package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableCell extends ASTNode {
  public TableCell(final ASTNode body) {
    super("td", body);
  }
}
