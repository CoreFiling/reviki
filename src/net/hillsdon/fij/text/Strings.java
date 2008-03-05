package net.hillsdon.fij.text;

import java.util.Iterator;

/**
 * As Collections is to Collection so Strings is to String... 
 */
public class Strings {
  
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
  
}
