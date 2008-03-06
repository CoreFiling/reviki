package net.hillsdon.reviki.wiki.renderer.result;

import java.util.Collections;
import java.util.List;


public class CompositeResultNode implements ResultNode {
  
  private final List<ResultNode> _children;

  public CompositeResultNode(final List<ResultNode> children) {
    _children = children;
  }

  public String toXHTML() {
    String result = "";
    for (ResultNode t : _children) {
      result += t.toXHTML();
    }
    return result;
  }

  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(_children);
  }

}
