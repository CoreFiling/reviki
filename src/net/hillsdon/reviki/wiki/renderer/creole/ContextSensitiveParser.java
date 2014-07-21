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
  /**
   * Whether to allow linebreaks in the current context or not.
   */
  protected Stack<Boolean> breaks = new Stack<Boolean>();

  public ContextSensitiveParser(TokenStream input) {
    super(input);
    breaks.push(new Boolean(true));
  }

  /**
   * Set the linebreak mode
   */
  protected void setBreaks(boolean breaks) {
    this.breaks.push(new Boolean(breaks));
  }

  /**
   * Check if we can break
   */
  protected boolean canBreak() {
    return breaks.peek().booleanValue();
  }

  /**
   * Revert to the prior linebreak mode. The default mode is to allow breaks.
   */
  protected void unsetBreaks() {
    breaks.pop();

    if (breaks.empty()) {
      breaks.push(new Boolean(true));
    }
  }
  
  /** See {@link #setBreaks(boolean)} */
  protected void yesBreak() {
    setBreaks(true);
  }
  
  /** See {@link #setBreaks(boolean)} */
  protected void noBreak() {
    setBreaks(false);
  }
}
