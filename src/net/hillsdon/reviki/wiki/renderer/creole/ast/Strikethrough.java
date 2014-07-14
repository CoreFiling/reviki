package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Strikethrough extends ASTNode {
  public Strikethrough(ASTNode body) {
    super("strike", body);
  }
}
