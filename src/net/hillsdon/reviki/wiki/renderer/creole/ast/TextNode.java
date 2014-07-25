package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.fij.text.Escape;

/**
 * Abstract class for AST nodes which just contain text.
 *
 * @author msw
 */
public abstract class TextNode extends ASTNode {
  private String _contents;

  private boolean _escape;

  public TextNode(final String contents, final boolean escape) {
    _contents = contents;
    _escape = escape;
  }

  @Override
  public String toXHTML() {
    return _escape ? Escape.html(_contents) : _contents;
  }

  public String getText() {
    return _contents;
  }
}
