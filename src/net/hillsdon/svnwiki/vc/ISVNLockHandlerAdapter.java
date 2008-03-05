package net.hillsdon.svnwiki.vc;

import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.io.ISVNLockHandler;

public class ISVNLockHandlerAdapter implements ISVNLockHandler {
  public void handleLock(String path, SVNLock lock, SVNErrorMessage error) throws SVNException {
  }
  public void handleUnlock(String path, SVNLock lock, SVNErrorMessage error) throws SVNException {
  }
}
