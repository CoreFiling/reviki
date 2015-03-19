package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

public class Nowiki extends TextNode {
  private final String _contents;

  public Nowiki(final String contents) {
    super(contents, true);

    _contents = contents;
  }

  @Override
  public List<ASTNode> expandMacrosInt(final Supplier<List<Macro>> macros) {
    return ImmutableList.of((ASTNode) this);
  }

  @Override
  public String getText() {
    return _contents;
  }
}
