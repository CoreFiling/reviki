package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.misc.Interval;

/**
 * An ANTLR lexer augmented with methods to ease context-sensitive actions.
 * 
 * @author msw
 */
public abstract class ContextSensitiveLexer extends Lexer {
  /**
   * Structure to represent a piece of inline formatting, such as bold or
   * italic, where the start and end symbols are the same, and nesting of the
   * same type is not allowed.
   */
  public class Formatting {
    /** The start/end string */
    String symbol;

    /** Whether we're currently inside it or not */
    boolean active;

    public Formatting(String symbol) {
      this.symbol = symbol;
      this.active = false;
    }
  }

  /**
   * The list of inline formatting tokens.
   */
  List<Formatting> inlineFormatting = new ArrayList<Formatting>();

  public ContextSensitiveLexer(CharStream input) {
    super(input);
    setupFormatting();
  }

  /**
   * Get a subsection of the input stream. This does NOT do bounds checking, and
   * so may throw an exception.
   * 
   * @param offset Offset relative to the current position.
   * @param len Length of the substring to get.
   * @return A substring of the given length starting at the appropriate
   *         position.
   */
  public String get(int offset, int len) {
    return _input.getText(new Interval(_input.index() + offset, _input.index() + offset + len - 1));
  }

  /**
   * Helper method for {@link #get(int, int)}, which gets a single character.
   */
  public String get(int offset) {
    return get(offset, 1);
  }

  /**
   * Helper method for {@link #get(int, int)}, which gets the next few
   * characters.
   */
  public String next(int len) {
    return get(0, len);
  }

  /**
   * Helper method for {@link #next(int)} which gets the next character.
   */
  public String next() {
    return next(1);
  }

  /**
   * Helper method for {@link #get(int)}, which gets the character prior to the
   * token.
   */
  public String prior() {
    return (_input.index() > getText().length()) ? get(-getText().length() - 1) : "";
  }

  /**
   * Seek the input stream, relative to the current position.
   *
   * @param amount The amount to seek by.
   */
  public void seek(int amount) {
    _input.seek(_input.index() + amount);
  }

  /**
   * Reads ahead in the input stream (as far as it needs) to see if the given
   * target string occurs before the given limit string.
   * 
   * @param target The string being sought out.
   * @param limit The "failure" string to match.
   * @return True if and only if the target string occurs before the limit
   *         string in the rest of the input stream. Hitting EOF counts as a
   *         failure.
   */
  public boolean findBefore(String target, String limit) {
    int ilen = _input.size() - _input.index();
    int tlen = target.length();
    int llen = limit.length();

    for (int i = 0; i < ilen - tlen; i++) {
      if (target.equals(get(i, tlen))) {
        return true;
      }
      else if (limit.equals(get(i, llen))) {
        return false;
      }
    }

    return false;
  }

  /**
   * Check whether a string is the start token of some inline formatting, or if
   * it's just some plain text.
   * 
   * That is, if bold is "++" and italic is "//", it lets us determine that the
   * first "//" in "++//foo++//" is NOT an italic opening mark.
   * 
   * @param formatting The formatting mark which has possibly been found.
   * @return True if and only if the closing formatting mark occurs before any
   *         other closing marks for formatting which was already open.
   */
  public boolean checkInline(Formatting formatting) {
    for (Formatting fmat : inlineFormatting) {
      if (fmat == formatting) {
        continue;
      }

      if (fmat.active && !findBefore(formatting.symbol, fmat.symbol)) {
        return false;
      }
    }

    for (String ender : thisKillsTheFormatting()) {
      if (ender != null && !findBefore(formatting.symbol, ender)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Check if a string is the start some inline formatting, and activate it if
   * so. If it's not, set the token type to the provided fallback value.
   * 
   * @param formatting The possible formatting we've found.
   * @param fallback The fallback token type.
   */
  public void setFormatting(Formatting formatting, int fallback) {
    if (checkInline(formatting)) {
      formatting.active = true;
    }
    else {
      setType(fallback);
    }
  }

  /**
   * Disable some active formatting.
   * 
   * @param formatting The formatting we've just exited.
   */
  public void unsetFormatting(Formatting formatting) {
    formatting.active = false;
  }

  /**
   * Turn off all formatting.
   */
  public void resetFormatting() {
    for (Formatting fmat : inlineFormatting) {
      unsetFormatting(fmat);
    }
  }

  /**
   * Check that the two characters immediately bracketing the current token do
   * NOT match the given patterns.
   *
   * @param forbiddenFst The pattern to check the first character against.
   * @param forbiddenSnd The pattern to check the second character against.
   * @return True if and only if the character immediately before and the
   *         character immediately after do not match it.
   */
  public boolean checkBounds(String forbiddenFst, String forbiddenSnd) {
    return !(prior().matches(forbiddenFst) || next().matches(forbiddenSnd));
  }

  /** See {@link #checkBounds(String, String)} */
  public boolean checkBounds(String forbidden) {
    return checkBounds(forbidden, forbidden);
  }

  /**
   * Set up any inline formatting.
   */
  public abstract void setupFormatting();

  /**
   * Get a list of strings which can end inline formatting at this point.
   */
  public abstract String[] thisKillsTheFormatting();
}
