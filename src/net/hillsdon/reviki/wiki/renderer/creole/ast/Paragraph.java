package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Paragraph extends TaggedNode {
  public Paragraph(final ASTNode body) {
    super("p", body);
  }
}
