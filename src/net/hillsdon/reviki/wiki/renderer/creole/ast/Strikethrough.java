package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Strikethrough extends TaggedNode {
  public Strikethrough(final ASTNode body) {
    super("strike", body);
  }
}
