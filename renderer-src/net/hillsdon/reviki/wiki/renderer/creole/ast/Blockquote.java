package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Blockquote extends ASTNode {
  public Blockquote(final ASTNode quoted) {
    super(quoted);

    _isBlock = true;
    _canContainBlock = true;
  }
}
