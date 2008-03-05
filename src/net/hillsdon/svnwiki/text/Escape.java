package net.hillsdon.svnwiki.text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Escape {

  public static String url(final String content) {
    try {
      return URLEncoder.encode(content, "UTF-8");
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
