package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;
import java.util.List;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.uwyn.jhighlight.renderer.Renderer;

public class Code extends ASTNode {
  public Code(final String contents) {
    super(new Raw(Escape.html(contents)));
  }

  public Code(final String contents, final Renderer highlighter) throws IOException {
    super(new Raw(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "")));
  }

  @Override
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    return this;
  }
}
