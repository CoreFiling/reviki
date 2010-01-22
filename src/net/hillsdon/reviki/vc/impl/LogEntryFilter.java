package net.hillsdon.reviki.vc.impl;

/**
 * Filters log entries.
 *  
 * @author mth
 */
public enum LogEntryFilter {

  PATH_ONLY {
    @Override
    public boolean accept(String fullLoggedPath, String changedPath) {
      return fullLoggedPath.equals(changedPath);
    }
  },
  ALL {
    @Override
    public boolean accept(String fullLoggedPath, String changedPath) {
      return true;
    }
  };

  public abstract boolean accept(String fullLoggedPath, String changedPath);
  
}
