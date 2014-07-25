package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableCell extends TaggedNode {
  public TableCell(final ASTNode body) {
    super("td", body);
  }
}
