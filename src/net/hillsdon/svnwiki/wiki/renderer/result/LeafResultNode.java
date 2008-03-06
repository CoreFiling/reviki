package net.hillsdon.svnwiki.wiki.renderer.result;

import java.util.Collections;
import java.util.List;


public abstract class LeafResultNode implements ResultNode {

  public final List<ResultNode> getChildren() {
    return Collections.emptyList();
  }
  
}
