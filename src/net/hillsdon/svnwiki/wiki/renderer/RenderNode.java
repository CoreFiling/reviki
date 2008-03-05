package net.hillsdon.svnwiki.wiki.renderer;

import java.util.List;
import java.util.regex.Matcher;

public interface RenderNode {

  /**
   * @return Child nodes as set in {@link #setChildren(RenderNode...)},
   *         the default is the empty list.
   */
  List<RenderNode> getChildren();
  
  /**
   * @param nodes Child nodes, matches will be attempted in the order given,
   *              giving a priority to earlier rules in case of equal match
   *              indices.
   *              
   * @return this, for conviniene.
   */
  RenderNode setChildren(RenderNode... nodes);

  /**
   * Render starting from this node.
   * @param text Input text.
   * @return Rendered HTML.
   */
  String render(String text);
  
  /**
   * Test for a match in the given text.
   * 
   * @param text The text.
   * @return A matcher if a match was found, null otherwise.
   */
  Matcher find(String text);

  /**
   * @param matcher A matcher that found a match using our find method and we were deemed the best.
   * @return Replacement text for the match (this method should recurse to complete rendering of the match).
   */
  String handle(Matcher matcher);

}
