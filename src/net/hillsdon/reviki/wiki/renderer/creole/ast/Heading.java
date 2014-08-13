package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Heading extends TaggedNode {
  private final int _level;

  public Heading(final int level, final ASTNode body) {
    super("h" + level, body);
    _isBlock = true;

    _level = level;
  }

  @Override
  protected ASTNode conjure(List<ASTNode> children) {
    return new Heading(_level, children.get(0));
  }
}
