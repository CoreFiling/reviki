package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Heading extends ASTNode {
  public Heading(int level, ASTNode body) {
    super("h" + level, body);
  }
}
