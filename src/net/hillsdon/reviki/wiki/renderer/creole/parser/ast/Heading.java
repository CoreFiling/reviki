package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Heading implements ResultNode {
  protected int level;
  protected ResultNode body;
  
  public Heading(int level, ResultNode body) {
    this.level = level;
    this.body = body;
  }
  
  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    out.add(body);
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    return "<h" + level + ">" + body.toXHTML() + "</h" + level + ">"; 
  }
}
