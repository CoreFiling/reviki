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
import java.util.Iterator;


/**
 * As Collections is to Collection so Strings is to String... 
 */
public class Strings {

  private static final String UTF8_ENCODING = "UTF8";
  public static final String CRLF = "\r\n";

  public static String sCharAt(String s, int index) {
    if (index >= 0 && index < s.length()) {
      return String.valueOf(s.charAt(index));
    }
    return "";
  }
  
  public static String join(final Iterator<?> iter, final String between) {
    return join(iter, null, null, between);
  }
  
  public static String join(final Iterator<?> iter, final String before, final String after, final String between) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    while (iter.hasNext()) {
      if (between != null) {
        if (first) {
          first = false;
        }
        else {
          sb.append(between);
        }
      }
      if (before != null) {
        sb.append(before);
      }
      sb.append(iter.next());
      if (after != null) {
        sb.append(after);
      }
    }
    return sb.toString();
  }

  public static String toUTF8(final byte[] bytes) {
    try {
      return new String(bytes, Strings.UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

  public static byte[] fromUTF8(final String string) {
    try {
      return string.getBytes(Strings.UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      throw new AssertionError("Java supports UTF8.");
    }
  }

}
