package net.hillsdon.svnwiki.vc;

import org.tmatesoft.svn.core.SVNException;

public class AlreadyLockedException extends PageStoreException {

  private static final long serialVersionUID = 1L;

  public AlreadyLockedException(final SVNException ex) {
    super(ex);
  }

}
