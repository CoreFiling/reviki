package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.io.IOException;

import com.uwyn.jhighlight.renderer.Renderer;

import net.hillsdon.fij.text.Escape;

public class InlineCode extends ASTNode {
  private String contents;

  private Renderer highlighter;

  public InlineCode(String contents) {
    super("code", new Raw(Escape.html(contents)));

    this.contents = contents;
    this.highlighter = null;
  }

  public InlineCode(String contents, Renderer highlighter) throws IOException {
    super("code", new Raw(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "")));

    this.contents = contents;
    this.highlighter = highlighter;
  }

  public Code toBlock() {
    if (highlighter == null) {
      return new Code(contents);
    }
    else {
      try {
        return new Code(contents, highlighter);
      }
      catch (IOException e) {
        return new Code(contents);
      }
    }
  }
}
