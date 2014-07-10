package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class NoWiki implements ResultNode {
  protected String body;

  public NoWiki(String contents) {
    this.body = contents;
  }

  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    return "<pre>" + Escape.html(body) + "</pre>";
  }
}
