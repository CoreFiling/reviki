package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Inline implements ResultNode {

  protected List<ResultNode> chunks;
  
  public Inline(List<ResultNode> chunks) {
    this.chunks = chunks;
  }
  
  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(chunks);
  }

  public String toXHTML() {
    String out = "";
    
    for(ResultNode node : chunks) {
      out = out + node.toXHTML();
    }
    
    return out;
  }
}
