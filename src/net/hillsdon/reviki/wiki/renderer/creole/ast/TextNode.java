package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.lang.reflect.Constructor;

/**
 * Abstract class for AST nodes which just contain text.
 *
 * @author msw
 */
public abstract class TextNode extends ASTNode {
  private final String _contents;

  private final boolean _escape;

  public TextNode(final String contents, final boolean escape) {
    _contents = contents;
    _escape = escape;
  }

  /**
   * Get the (unescaped) text contained within this node.
   *
   * @return
   */
  public String getText() {
    return _contents;
  }

  /**
   * Get whether the contents should be escaped.
   */
  public boolean isEscaped() {
    return _escape;
  }

  /**
   * Construct a new TextNode of the same type by appending the text of the
   * follower to this.
   */
  public TextNode append(final String more) {
    String text = _contents + more;

    // First try a constructor which just takes a string.
    try {
      Constructor<? extends TextNode> constructor = getClass().getDeclaredConstructor(String.class);
      return constructor.newInstance(text);
    }

    // Fall back to the full constructor if there's no more specific
    // overridden one.
    catch (Exception e) {
      try {
        Constructor<? extends TextNode> constructor = getClass().getDeclaredConstructor(String.class, boolean.class);
        return constructor.newInstance(text, _escape);
      }

      // See similar comments about judgement and shaming of subclass authors in
      // ASTNode::expandMacros.
      catch (Exception e2) {
        throw new RuntimeException(e2);
      }
    }
  }

  /**
   * Construct a new TextNode of the same type by appending the text of the
   * follower to this.
   */
  public TextNode append(final TextNode follower) {
    return append(follower.getText());
  }

  @Override
  protected void toSmallString(StringBuilder sb) {
    sb.append(getText());
  }
}
