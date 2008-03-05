/**
 * 
 */
package net.hillsdon.svnwiki.vc;

import java.io.IOException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.SVNRepository;

public interface SVNAction<T> {
  T perform(SVNRepository repository) throws SVNException, PageStoreException, IOException;
}