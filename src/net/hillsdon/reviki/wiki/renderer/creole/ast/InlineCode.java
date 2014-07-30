package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Supplier;
import com.uwyn.jhighlight.renderer.Renderer;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class InlineCode extends TextNode implements BlockableNode<Code> {
  private final String _contents;

  private final Renderer _highlighter;

  public InlineCode(final String contents) {
    super(contents, true);

    _contents = contents;
    _highlighter = null;
  }

  public InlineCode(final String contents, final Renderer highlighter) throws IOException {
    super(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", ""), false);

    _contents = contents;
    _highlighter = highlighter;
  }

  public Code toBlock() {
    if (_highlighter == null) {
      return new Code(_contents);
    }
    else {
      try {
        return new Code(_contents, _highlighter);
      }
      catch (IOException e) {
        return new Code(_contents);
      }
    }
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
