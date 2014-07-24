package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;

import com.uwyn.jhighlight.renderer.Renderer;

import net.hillsdon.fij.text.Escape;

public class InlineCode extends BlockableNode<Code> {
  private String _contents;

  private Renderer _highlighter;

  public InlineCode(final String contents) {
    super("code", new Raw(Escape.html(contents)));

    _contents = contents;
    _highlighter = null;
  }

  public InlineCode(final String contents, final Renderer highlighter) throws IOException {
    super("code", new Raw(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "")));

    _contents = contents;
    _highlighter = highlighter;
  }

  @Override
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
}
