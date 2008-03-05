package net.hillsdon.svnwiki.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with wiki-words.
 *
 * @copyright
 * @author mth
 */
public final class WikiWordUtils {

  /**
   * Takes the last '/' component of a '/' separated path and splits the last
   * component as a wiki word.
   * 
   * @param path The path.
   * @return e.g. "foo/BarHumbug" gives "Bar humbug".
   */
  public static CharSequence pathToTitle(final String path) {
    List<String> parts = splitCamelCase(path.substring(path.lastIndexOf('/') + 1));
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < parts.size(); ++i) {
      String part = parts.get(i);
      if (part.length() > 0) {
        if (out.length() > 0) {
          out.append(' ');
        }
        if (i == 0) {
          out.append(Character.toUpperCase(part.charAt(0)));
        }
        else {
          out.append(Character.toLowerCase(part.charAt(0)));
        }
        out.append(part.substring(1));
      }
    }
    return out;
  }

  /**
   * @param text Some text.
   * @return true if it looks like a wiki word.
   */
  public static boolean isWikiWord(final String text) {
    return text.split("\\s").length == 1 && !isNextLower(text.toCharArray(), -1) && splitCamelCase(text).size() > 1; 
  }
  
  /**
   * Splits camel case.
   * 
   * Behaviour undefined for strings containing punctuation/whitespace.
   * Note that here uppercase is defined as !Character.isLowerCase(char)
   * notably, this includeds digits.
   */
  public static List<String> splitCamelCase(final String in) {
    List<String> result = new ArrayList<String>();
    char[] chars = in.toCharArray();
    int takenLength = 0;
    boolean lastLower = false;
    boolean nextLower = isNextLower(chars, -1);
    for (int i = 0; i < chars.length; ++i) {
      boolean currentUpper = !nextLower;
      boolean lastUpper = !lastLower && i > 0;
      nextLower = isNextLower(chars, i);

      // Step up is e.g. aB, step down is Ab.
      boolean stepUp = lastLower && currentUpper;
      boolean nextStepDown = lastUpper && currentUpper && nextLower;
      if (stepUp || nextStepDown) {
        result.add(in.substring(takenLength, i));
        takenLength = i;
      }
      lastLower = !currentUpper;
    }
    result.add(in.substring(takenLength));
    return result;
  }

  private static boolean isNextLower(char[] chars, int currentPos) {
    if (currentPos + 1 < chars.length) {
      char c = chars[currentPos + 1];
      return  Character.isLowerCase(c);
    }
    return false;
  }

}
