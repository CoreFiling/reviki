package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Heading extends ASTNode {
  private final int _level;

  public Heading(final int level, final ASTNode body) {
    super(body);

    _isBlock = true;
    _level = level;
  }

  public int getLevel() {
    return _level;
  }

  protected ASTNode conjure(List<ASTNode> children) {
    return new Heading(_level, children.get(0));
  }
}
