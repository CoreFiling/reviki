package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

public class Paragraph extends ASTNode {
  public Paragraph(ASTNode body) {
    super("p", body);
  }
}
