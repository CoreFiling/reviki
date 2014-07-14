package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Bold extends ASTNode {
  public Bold(ASTNode body) {
    super("strong", body);
  }
}
