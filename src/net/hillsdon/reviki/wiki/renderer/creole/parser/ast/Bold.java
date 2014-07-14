package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

public class Bold extends ASTNode {
  public Bold(ASTNode body) {
    super("strong", body);
  }
}
