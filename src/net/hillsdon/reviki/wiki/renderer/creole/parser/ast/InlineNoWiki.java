package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class InlineNoWiki implements ResultNode {
  protected String body;
  
  public InlineNoWiki(String contents) {
    this.body = contents;
  }
  
  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    return "<code>" + body + "</code>"; 
  }
}
