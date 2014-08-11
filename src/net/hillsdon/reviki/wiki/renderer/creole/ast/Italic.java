package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Italic extends TaggedNode {
  public Italic(final ASTNode body) {
    super("em", body);
  }
}
