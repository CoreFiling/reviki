package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Heading extends TaggedNode {
  public Heading(final int level, final ASTNode body) {
    super("h" + level, body);
  }
}
