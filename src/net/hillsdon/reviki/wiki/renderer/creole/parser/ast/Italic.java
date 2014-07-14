package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

public class Italic extends ASTNode {
  public Italic(ASTNode body) {
    super("em", body);
  }
}
