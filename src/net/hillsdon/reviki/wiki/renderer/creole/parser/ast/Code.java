package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

import com.uwyn.jhighlight.renderer.Renderer;

public class Code implements ResultNode {
  protected String body;

  private Renderer highlighter;

  public Code(String contents) {
    this(contents, null);
  }

  public Code(String contents, Renderer highlighter) {
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
        out = highlighter.highlight("", body, "UTF-8", true).replace("&nbsp;", " ").replace("<br />", "");
      }
      catch (Exception e) {
        out = Escape.html(body);
      }
    }

    return "<pre>" + out + "</pre>";
  }
}
