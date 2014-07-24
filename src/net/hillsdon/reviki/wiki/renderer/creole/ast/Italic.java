package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Italic extends ASTNode {
  public Italic(final ASTNode body) {
    super("em", body);
  }
}
