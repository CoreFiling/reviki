package net.hillsdon.svnwiki.wiki.renderer;

import java.util.Collections;
import java.util.List;

import net.hillsdon.svnwiki.wiki.renderer.creole.ResultNode;

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
