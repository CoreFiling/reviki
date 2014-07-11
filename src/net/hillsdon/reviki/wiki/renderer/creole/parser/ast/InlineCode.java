package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.uwyn.jhighlight.renderer.Renderer;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class InlineCode implements ResultNode {
  protected String body;

  private Renderer highlighter;

  public InlineCode(String contents) {
    this(contents, null);
  }

  public InlineCode(String contents, Renderer highlighter) {
    this.body = contents;
    this.highlighter = highlighter;
  }

  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    String out;

    if (highlighter == null) {
      out = Escape.html(body);
    }
    else {
      try {
        out = highlighter.highlight("", body, "UTF-8", true).replace("&bnsp;", " ");
      }
      catch (Exception e) {
        out = Escape.html(body);
      }
    }

    return "<code>" + out + "</code>";
  }

  public Code toBlock() {
    return new Code(body, highlighter);
  }
}
