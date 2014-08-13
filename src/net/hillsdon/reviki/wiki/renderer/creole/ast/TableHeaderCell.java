package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class TableHeaderCell extends TaggedNode {
  public TableHeaderCell(final List<ASTNode> contents) {
    super("th", contents);

    _isBlock = true;
    _canContainBlock = true;
  }
}
