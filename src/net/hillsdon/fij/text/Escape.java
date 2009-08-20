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
package net.hillsdon.fij.text;

import java.io.UnsupportedEncodingException;


public class Escape {
  
  /**
   * Avoid encoding an entire link that you wish a browser to use,
   * such as "http://....". Instead, encode only what is required.
   * For example:
   * <ul>
   * <li>urlEncoder.encodeURL(pagesRoot() + Escape.encodeUTF8(name));</li>
   * </ul>
   * @param content
   * @return
   */
  public static String urlEncodeUTF8(String content) {
    try {
      return java.net.URLEncoder.encode(content, "UTF-8");
    }
    catch (UnsupportedEncodingException ex) {
      throw new Error("Java supports UTF-8!", ex);
    }
  }

  /**
   * HTML escaping routine.  This is a bit extreme for element content.
   * 
   * @param content
   *          the unescaped content.
   * @return the escaped output.
   */
  public static String html(final String content) {
    if (content == null) {
      return "";
    }
    char[] chars = content.toCharArray();
    final StringBuffer result = new StringBuffer(2 * chars.length);
    for (int i = 0; i < chars.length; ++i) {
      char character = chars[i];
      if (character == '<') {
        result.append("&lt;");
      }
      else if (character == '>') {
        result.append("&gt;");
      }
      else if (character == '&') {
        result.append("&amp;");
      }
      else if (character == '\"') {
        result.append("&quot;");
      }
      else if (character == '\'') {
        result.append("&#039;");
      }
      else if (character == '\\') {
         result.append("&#092;");
      }
      else {
        result.append(character);
      }
    }
    return result.toString();
  }

}
