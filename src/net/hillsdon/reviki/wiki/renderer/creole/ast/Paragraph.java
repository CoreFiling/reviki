package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Paragraph extends ASTNode {
  public Paragraph(final ASTNode body) {
    super("p", body);
  }
}
