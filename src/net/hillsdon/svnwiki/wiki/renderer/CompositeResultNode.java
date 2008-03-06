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
    return asText(_children);
  }

  public List<ResultNode> getChildren() {
    return Collections.unmodifiableList(_children);
  }

  public static String asText(final Iterable<ResultNode> children) {
    String result = "";
    for (ResultNode t : children) {
      result += t.toXHTML();
    }
    return result;
  }

}
