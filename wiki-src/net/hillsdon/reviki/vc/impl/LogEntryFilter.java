package net.hillsdon.reviki.vc.impl;

import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;

/**
 * Filters log entries.
 *  
 * @author mth
 */
public enum LogEntryFilter {

  PATH_ONLY {
    @Override
    public boolean accept(String fullLoggedPath, SVNLogEntryPath changedPath) {
      return fullLoggedPath.equals(changedPath.getPath());
    }
  },
  DESCENDANTS {
    @Override
    public boolean accept(String fullLoggedPath, SVNLogEntryPath changedPath) {
      return SVNPathUtil.isWithinBasePath(fullLoggedPath, changedPath.getPath());
    }
  };

  public abstract boolean accept(String fullLoggedPath, SVNLogEntryPath changedPath);
  
}
