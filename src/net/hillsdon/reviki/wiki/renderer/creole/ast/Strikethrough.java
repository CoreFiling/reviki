package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Strikethrough extends ASTNode {
  public Strikethrough(final ASTNode body) {
    super("strike", body);
  }
}
