package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class RenderedHorizontalRule implements ResultNode {
  public List<ResultNode> getChildren() {
    List<ResultNode> out = new ArrayList<ResultNode>();
    return Collections.unmodifiableList(out);
  }

  public String toXHTML() {
    return "<hr>"; 
  }
}
