package net.hillsdon.fij.io;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class Lsof {

  /**
   * @return Set of known open files (canonical file objects), falling back to empty set.  Linux only for now.
   *         Useful for asserting a file has been closed.
   */
  public static Set<File> lsof() {
    Set<File> files = new LinkedHashSet<File>();
    File fds = new File("/proc/self/fd");
    if (fds.exists()) {
      for (File fd : fds.listFiles()) {
        try {
          files.add(fd.getCanonicalFile());
        }
        catch (IOException ignore) {
        }
      }
    }
    return files;
  }

  
}
