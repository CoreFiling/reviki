package net.hillsdon.fij.io;

import java.io.File;
import java.util.Iterator;

import net.hillsdon.fij.text.Strings;

import static net.hillsdon.fij.core.Functional.iter;

/**
 * Path manipulation.
 * 
 * @author mth
 */
public final class Path {

  /**
   * Joins with File.separator.
   */
  public static String join(final String... paths) {
    return join(iter(paths));
  }
  
  /**
   * Joins with File.separator.
   */
  public static String join(final Iterator<String> paths) {
    return Strings.join(paths, File.separator);
  }

  private Path() {
  }
  
}
