package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.Stack;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

/**
 * An ANTLR parser augmented with methods to make context sensitivity easier.
 *
 * @author msw
 */
public abstract class ContextSensitiveParser extends Parser {
  private final Stack<Boolean> _breaks = new Stack<Boolean>();

  public ContextSensitiveParser(final TokenStream input) {
    super(input);
  }

  /** Check if we can break. The default is yes. */
  public boolean canBreak() {
    return _breaks.isEmpty() || _breaks.peek().booleanValue();
  }

  /** Revert to the prior linebreak mode. */
  public void unsetBreaks() {
    _breaks.pop();
  }

  /** Allow breaks in this context. */
  public void allowBreaks() {
    _breaks.push(new Boolean(true));
  }

  /** See {@link #setBreaks(boolean)}. */
  protected void disallowBreaks() {
    _breaks.push(new Boolean(false));
  }
}
