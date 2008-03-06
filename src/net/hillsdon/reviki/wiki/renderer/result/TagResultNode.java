package net.hillsdon.reviki.wiki.renderer.result;

import java.util.Collections;
import java.util.List;


/**
 * Outputs an XHTML tag.
 * 
 * @author mth
 */
public class TagResultNode extends CompositeResultNode {
  
  private final String _tag;

  public TagResultNode(final String tag) {
    this(tag, Collections.<ResultNode>emptyList());
  }

  public TagResultNode(final String tag, final List<ResultNode> children) {
    super(children);
    _tag = tag;
  }
  
  public String getTag() {
    return _tag;
  }
  
  public String toXHTML() {
    if (getChildren().isEmpty()) {
      return "<" + _tag + " />";
    }
    return "<" + _tag + ">" +  super.toXHTML() + "</" + _tag + ">";
  }

}
