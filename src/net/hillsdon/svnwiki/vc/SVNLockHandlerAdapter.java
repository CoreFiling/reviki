package net.hillsdon.svnwiki.vc;

import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.io.ISVNLockHandler;

/**
 * Empty implementation.
 * 
 * @author mth
 */
public class SVNLockHandlerAdapter implements ISVNLockHandler {
  public void handleLock(final String path, final SVNLock lock, final SVNErrorMessage error) throws SVNException {
  }
  public void handleUnlock(final String path, final SVNLock lock, final SVNErrorMessage error) throws SVNException {
  }
}
