package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;
import java.util.Map;

import net.hillsdon.fij.text.Escape;

public class Anchor extends TaggedNode {
  private final String _anchor;

  public Anchor(final String anchor) {
    super("a");

    _anchor = anchor;
  }

  @Override
  public String toXHTML(Map<String, List<String>> enabledDirectives) {
    return String.format("<a %s id='%s'></a>", CSS_CLASS_ATTR, Escape.html(_anchor));
  }
}
