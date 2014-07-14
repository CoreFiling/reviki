package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

public class TableCell extends ASTNode {
  public TableCell(ASTNode body) {
    super("td", body);
  }
}
