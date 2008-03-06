package net.hillsdon.svnwiki.wiki.renderer.creole;

import java.util.List;

/**
 * We encode the results of parsing wiki mark-up as a ResultNode tree.
 * 
 * The idea is we can encode useful information for analysis and
 * transformation but for now most of the nodes are generic and the
 * rendering work is still done in by the {@link RenderNode}s.
 * 
 * @author mth
 */
public interface ResultNode {

  List<ResultNode> getChildren();
  
  String toXHTML();

}
