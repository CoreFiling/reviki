package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;

public class Heading extends ASTNode {
  private final int _level;

  public Heading(final int level, final ASTNode body) {
    super(body);
    _level = level;
  }

  public int getLevel() {
    return _level;
  }

  @Override
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    ASTNode child = getChildren().get(0);
    ASTNode expanded = child.expandMacros(macros);

    if (child == expanded) {
      return this;
    }
    else {
      return new Heading(_level, expanded);
    }
  }
}
