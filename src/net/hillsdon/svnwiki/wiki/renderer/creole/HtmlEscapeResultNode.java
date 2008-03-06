package net.hillsdon.svnwiki.wiki.renderer.creole;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.wiki.renderer.LeafResultNode;

public class HtmlEscapeResultNode extends LeafResultNode {

  private final String _text;

  public HtmlEscapeResultNode(final String text) {
    _text = text;
  }
  
  public String toXHTML() {
    return Escape.html(_text);
  }

}
