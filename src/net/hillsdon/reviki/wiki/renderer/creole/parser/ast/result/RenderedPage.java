package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class RenderedPage implements ResultNode {

  protected List<ResultNode> blocks;
  
  public RenderedPage(List<ResultNode> blocks) {
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
