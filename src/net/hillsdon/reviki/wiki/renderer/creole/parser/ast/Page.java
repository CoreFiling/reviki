package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Page implements ResultNode {

  protected List<ResultNode> blocks;
  
  public Page(List<ResultNode> blocks) {
    this.blocks = blocks;
  }
  
  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(blocks);
  }

  public String toXHTML() {
    String out = "";
    
    for(ResultNode node : blocks) {
      out += node.toXHTML();
    }
    
    return out;
  }

}
