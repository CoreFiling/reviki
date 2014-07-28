package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.fij.text.Escape;

public class Anchor extends TaggedNode {
  private final String _anchor;

  public Anchor(final String anchor) {
    super("a");

    _anchor = anchor;
  }

  @Override
  public String toXHTML() {
    return String.format("<a %s id='%s'></a>", CSS_CLASS_ATTR, Escape.html(_anchor));
  }
}
