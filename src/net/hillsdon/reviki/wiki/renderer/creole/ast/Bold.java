package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Bold extends TaggedNode {
  public Bold(final ASTNode body) {
    super("strong", body);
  }
}
