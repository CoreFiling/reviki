package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.uwyn.jhighlight.renderer.Renderer;

public class Code extends TextNode {
  private final String _contents;

  private final Renderer _highlighter;

  public Code(final String contents) {
    super(contents, true);

    _contents = contents;
    _highlighter = null;
  }

  public Code(final String contents, final Renderer highlighter) throws IOException {
    super(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", ""), false);

    _contents = contents;
    _highlighter = highlighter;
  }

  @Override
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    return this;
  }

  @Override
  public String getText() {
    return _contents;
  }

  public Renderer getHighlighter() {
    return _highlighter;
  }
}
