package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Paragraph extends ASTNode {
  public Paragraph(ASTNode body) {
    super("p", body);
  }
}
