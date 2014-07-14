package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Italic extends ASTNode {
  public Italic(ASTNode body) {
    super("em", body);
  }
}
