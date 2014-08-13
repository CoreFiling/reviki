package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;
import java.util.List;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.uwyn.jhighlight.renderer.Renderer;

public class Code extends TaggedNode {
  public Code(final String contents) {
    super("pre", new Raw(Escape.html(contents)));
  }

  public Code(final String contents, final Renderer highlighter) throws IOException {
    super("pre", new Raw(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "")));
  }

  @Override
  public List<ASTNode> expandMacrosInt(final Supplier<List<Macro>> macros) {
    return ImmutableList.of((ASTNode) this);
  }
}
