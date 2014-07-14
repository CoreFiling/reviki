package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.io.IOException;

import net.hillsdon.fij.text.Escape;

import com.uwyn.jhighlight.renderer.Renderer;

public class Code extends ASTNode {
  public Code(String contents) {
    super("pre", new Raw(Escape.html(contents)));
  }

  public Code(String contents, Renderer highlighter) throws IOException {
    super("pre", new Raw(highlighter.highlight("", contents, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "")));
  }
}
