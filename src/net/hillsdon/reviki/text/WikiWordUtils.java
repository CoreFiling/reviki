/**
 * Copyright 2007 Matthew Hillsdon
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
   * Takes the last '/' component of a '/' separated path and splits the last
   * component as a wiki word.
   * 
   * @param path The path.
   * @return e.g. "foo/BarHumbug" gives "Bar humbug".
   */
  public static String pathToTitle(final String path) {
    List<String> parts = splitCamelCase(path.substring(path.lastIndexOf('/') + 1));
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < parts.size(); ++i) {
      String part = parts.get(i);
      if (part.length() > 0) {
        if (out.length() > 0) {
          out.append(' ');
        }
        out.append(part);
      }
    }
    return out.toString();
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
    return text.split("\\s").length == 1;
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

  private static boolean isNextLower(final char[] chars, final int currentPos) {
    if (currentPos + 1 < chars.length) {
      char c = chars[currentPos + 1];
      return  Character.isLowerCase(c);
    }
    return false;
  }

  public static boolean isAcronym(final String pageName) {
    return pageName != null && pageName.matches("\\p{Lu}+");
  }

}
