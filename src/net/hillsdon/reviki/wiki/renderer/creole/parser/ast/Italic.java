package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Italic implements ResultNode {
  protected ResultNode body;
  
  public Italic(ResultNode body) {
    this.body = body;
  }
  
  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(body);
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    return "<em>" + body.toXHTML() + "</em>"; 
  }
}