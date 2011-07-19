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

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;


public class Escape {
  
  /**
   * Generate a correctly encoded URI string from the given components.
   * 
   * @param path The unencoded path. Will be encoded according to RFC3986.
   * @param query The unencoded query. May be null. Will be x-www-form-urlencoded.
   * @param fragment The unencoded fragment. May be null. Will be encoded according to RFC3986.
   * @param extraPath The <strong>encoded</strong> extra part to append to the path.
   * @return
   */
  public static String constructEncodedURI(final String path, final String query, final String fragment, final String extraPath) {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(URIUtil.encodeWithinPath(path, "UTF-8"));
      if (extraPath != null) {
        sb.append(extraPath);
      }
      if (query != null) {
        sb.append("?");
        sb.append(URIUtil.encodeQuery(query, "UTF-8"));
      }
      if (fragment != null) {
        sb.append("#");
        sb.append(URIUtil.encodeWithinPath(fragment, "UTF-8"));
      }
      return sb.toString();
    }
    catch(URIException ex) {
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
