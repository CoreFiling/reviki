package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.uwyn.jhighlight.renderer.Renderer;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class InlineCode extends TaggedNode implements BlockableNode<Code> {
  private final String _contents;

  private final Renderer _highlighter;

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
  public List<ASTNode> expandMacrosInt(final Supplier<List<Macro>> macros) {
    return ImmutableList.of((ASTNode) this);
  }
}
