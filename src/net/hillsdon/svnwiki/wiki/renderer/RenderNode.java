package net.hillsdon.svnwiki.wiki.renderer;

import java.util.List;
import java.util.regex.Matcher;

public interface RenderNode {

  List<RenderNode> getChildren();
  RenderNode setChildren(final RenderNode... rules);

  String render(final String text);
  String handle(final RenderNode node, final Matcher matcher);
  Matcher find(final String text);

}
