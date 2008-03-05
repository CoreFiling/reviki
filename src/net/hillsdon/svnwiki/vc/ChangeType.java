package net.hillsdon.svnwiki.vc;

public enum ChangeType {
  
  MODIFIED, 
  ADDED , 
  DELETED,
  /** A delete and add in a single commit. */
  REPLACED;
  
  public static ChangeType forCode(final char type) {
    switch (type) {
      case 'M':
        return MODIFIED;
      case 'A':
        return ADDED;
      case 'D':
        return DELETED;
      case 'R':
        return REPLACED;
      default:
        throw new IllegalArgumentException("Unknown code: " + type);
    }
  }

}
