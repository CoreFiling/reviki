package net.hillsdon.svnwiki.wiki.renderer.creole;

import java.util.List;
import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;

public interface RenderNode {

  /**
   * @return Child nodes as set in {@link #addChildren(RenderNode...)},
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
  RenderNode addChildren(RenderNode... nodes);

  /**
   * Render starting from this node.
   * @param page TODO
   * @param text Input text.
   * @return Rendered HTML.
   */
  String render(PageReference page, String text);
  
  /**
   * Test for a match in the given text.
   * 
   * @param text The text.
   * @return A matcher if a match was found, null otherwise.
   */
  Matcher find(String text);

  /**
   * @param page TODO
   * @param matcher A matcher that found a match using our find method and we were deemed the best.
   * @return Replacement text for the match (this method should recurse to complete rendering of the match).
   */
  String handle(PageReference page, Matcher matcher);

}
