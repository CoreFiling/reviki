package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Blockquote extends TaggedNode {
  public Blockquote(final ASTNode quoted) {
    super("blockquote", quoted);
  }
}
