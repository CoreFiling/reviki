/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with WikiWords.
 *
 * @copyright
 * @author mth
 */
public final class WikiWordUtils {

  /**
   * Extracts the last component of a '/' separated path and splits the last
   * component as a wiki word.
   *
   * @param path The path.
   * @return e.g. "foo/BarHumbug" gives "Bar Humbug".
   */
  public static String pathToTitle(final String path) {
    final String last = lastComponentOfPath(path);
    final List<String> parts = splitCamelCase(last);
    StringBuilder out = new StringBuilder(2 * last.length());
    for (String part : parts) {
      if (part.length() > 0) {
        if (out.length() > 0) {
          out.append(' ');
        }
        out.append(part);
      }
    }
    return out.toString();
  }

  public static String lastComponentOfPath(final String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * @param text Some text.
   * @return true if it looks like a wiki word.
   */
  public static boolean isWikiWord(final String text) {
    return hasNoWhitespace(text) && startsUpperCase(text) && (isAbbreviation(text) || hasCamelCaseParts(text));
  }

  private static boolean hasCamelCaseParts(final String text) {
    List<String> parts = splitCamelCase(text);
    return parts.size() > 1;
  }

  private static boolean isAbbreviation(final String text) {
    return text.matches("\\p{Lu}{3,}");
  }

  private static boolean startsUpperCase(final String text) {
    return text.length() > 0 && Character.isUpperCase(text.charAt(0));
  }

  private static boolean hasNoWhitespace(final String text) {
    return text.split("\\s", -1).length == 1;
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
    boolean lastNeutral = false;
    boolean currentNeutral = false;
    for (int i = 0; i < chars.length; ++i) {
      boolean currentUpper = !nextLower;
      boolean lastUpper = !lastLower && i > 0;
      nextLower = isNextLower(chars, i);
      currentNeutral = isCurrentNeutral(chars, i);

      // Step up is e.g. aB, step down is Ab.
      boolean stepUp = lastLower && (currentUpper && !currentNeutral);
      boolean nextStepDown = (lastUpper && !lastNeutral) && (currentUpper && !currentNeutral) && nextLower;
      if (stepUp || nextStepDown) {
        result.add(in.substring(takenLength, i));
        takenLength = i;
      }
      lastLower = !currentUpper;
      lastNeutral = currentNeutral;
    }
    result.add(in.substring(takenLength));
    return result;
  }

  private static boolean isNextLower(final char[] chars, final int currentPos) {
    if (currentPos + 1 < chars.length) {
      char c = chars[currentPos + 1];
      return  Character.isLowerCase(c);
    }
    return false;
  }

  private static boolean isCurrentNeutral(final char[] chars, final int currentPos) {
    if (currentPos < chars.length) {
      char c = chars[currentPos];
      return  !Character.isLetterOrDigit(c);
    }
    return false;
  }

  public static boolean isAcronym(final String pageName) {
    return pageName != null && pageName.matches("\\p{Lu}+s?");
  }

}
