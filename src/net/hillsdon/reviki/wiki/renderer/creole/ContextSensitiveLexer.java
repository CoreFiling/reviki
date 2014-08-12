package net.hillsdon.reviki.wiki.renderer.creole;

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
    /** The start/end string. */
    String symbol;

    /** Whether we're currently inside it or not. */
    boolean active;

    /** The start token type. */
    final int start;

    /** The end token type. */
    final int end;

    public Formatting(final String symbol, final int start, final int end) {
      this.symbol = symbol;
      this.active = false;
      this.start = start;
      this.end = end;
    }
  }

  /**
   * The list of inline formatting tokens.
   */
  private final List<Formatting> _inlineFormatting;

  public ContextSensitiveLexer(final CharStream input) {
    super(input);
    _inlineFormatting = setupFormatting();
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
  public String get(final int offset, final int len) {
    return _input.getText(new Interval(_input.index() + offset, _input.index() + offset + len - 1));
  }

  /**
   * Helper method for {@link #get(int, int)}, which gets a single character.
   */
  public String get(final int offset) {
    return get(offset, 1);
  }

  /**
   * Helper method for {@link #get(int, int)}, which gets the next few
   * characters.
   */
  public String next(final int len) {
    return get(0, len);
  }

  /**
   * Helper method for {@link #next(int)} which gets the next character.
   */
  public Character next() {
    String nxt = next(1);
    return (nxt.length() > 0) ? nxt.charAt(0) : null;
  }

  /**
   * Helper method for {@link #get(int)}, which gets the character prior to the
   * token.
   */
  public Character prior() {
    int len = getText().length();
    return (_input.index() > len) ? get(-len - 1).charAt(0) : null;
  }

  /**
   * Helper method for {@link #get(int)}, which gets the character two prior to
   * the token.
   */
  public Character priorprior() {
    int len = getText().length() + 1;
    return (_input.index() > len) ? get(-len - 1).charAt(0) : null;
  }

  /**
   * Find the prior character on this line which was not whitespace. Returns
   * null if there is no such character.
   */
  public Character priorNonWS() {
    Character out = null;
    int len = getText().length();

    for (int i = 1; true; i++) {
      Character chr = get(-len - i).charAt(0);
      if (chr != ' ' && chr != '\t') {
        out = chr;
        break;
      }
    }

    return out;
  }

  /**
   * Helper method for {@link #get(int)}, which gets the last character of the
   * token.
   */
  public Character last() {
    return get(-1, 1).charAt(0);
  }

  /**
   * Seek the input stream, relative to the current position.
   *
   * @param amount The amount to seek by.
   */
  public void seek(final int amount) {
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
  public boolean findBefore(final String target, final String limit) {
    int ilen = _input.size() - _input.index();
    int tlen = target.length();
    int llen = limit.length();

    boolean inlink = false;
    boolean start = false;

    for (int i = 0; i < ilen - tlen; i++) {
      // Fast-forward the input stream over links, as special tokenisation rules
      // apply there
      String next = get(i, 2);
      if (next.equals("[[") || next.equals("{{")) {
        inlink = true;
      }

      if (inlink) {
        if (next.equals("]]") || next.equals("}}")) {
          inlink = false;
        }
        else {
          continue;
        }
      }

      // Keep track of whether we're at the start of a line or not.
      if (get(i).equals("\n") || start) {
        start = get(i).trim().equals("");
      }

      // See if we have a match for either string.
      if (target.equals(get(i, tlen))) {
        // Special case for italics: the "//" in "://" is not an italic symbol.
        if (target.equals("//")) {
          String before = get(i - 2, 2);
          if (before.endsWith(":") && Character.isLetter(before.charAt(0))) {
            continue;
          }
        }

        return true;
      }
      else {
        // \L, at the start of a limit, matches the start of a line.
        if (limit.startsWith("\\L") && start && limit.substring(2).equals(get(i + 1, llen - 2))) {
          break;
        }
        else if (limit.equals(get(i, llen))) {
          break;
        }
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
  public boolean checkInline(final Formatting formatting) {
    for (Formatting fmat : _inlineFormatting) {
      if (fmat == formatting) {
        continue;
      }

      if (fmat.active && !findBefore(formatting.symbol, fmat.symbol)) {
        return false;
      }
    }

    for (String ender : thisKillsTheFormatting()) {
      if (!findBefore(formatting.symbol, ender)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Toggle some formatting. If we think we've hit a start token, check if there
   * is an end token: if not, set the token type to the fallback value.
   *
   * @param formatting The formatting we think we've found
   * @param fallback The fallback token type
   */
  public void toggleFormatting(final Formatting formatting, final int fallback) {
    if (formatting.active) {
      formatting.active = false;
      setType(formatting.end);
    }
    else {
      if (checkInline(formatting)) {
        formatting.active = true;
        setType(formatting.start);
      }
      else {
        setType(fallback);
      }
    }
  }

  /**
   * Turn off all formatting.
   */
  public void resetFormatting() {
    for (Formatting fmat : _inlineFormatting) {
      fmat.active = false;
    }
  }

  /**
   * Set up any inline formatting.
   */
  public abstract List<Formatting> setupFormatting();

  /**
   * Get a list of strings which can end inline formatting at this point.
   */
  public abstract List<String> thisKillsTheFormatting();
}
