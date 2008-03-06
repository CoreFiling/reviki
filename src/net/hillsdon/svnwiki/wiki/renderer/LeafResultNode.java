package net.hillsdon.svnwiki.wiki.renderer;

import java.util.Collections;
import java.util.List;

import net.hillsdon.svnwiki.wiki.renderer.creole.ResultNode;

public abstract class LeafResultNode implements ResultNode {

  public final List<ResultNode> getChildren() {
    return Collections.emptyList();
  }
  
}
