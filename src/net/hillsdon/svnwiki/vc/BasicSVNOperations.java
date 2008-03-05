package net.hillsdon.svnwiki.vc;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * The low-level SVN operations in terms of our classes where useful.
 * 
 * This interface insulates the {@link SVNPageStore} from the {@link SVNRepository}
 * to enable testing of the logic in the page store.
 * 
 * @author mth
 */
public interface BasicSVNOperations {

  /**
   * Returns the most recent changes first.
   */
  List<ChangeInfo> log(String path, long limit, boolean pathOnly, long startRevision, long endRevision) throws PageStoreAuthenticationException, PageStoreException;

  Collection<String> listFiles(String dir) throws PageStoreAuthenticationException, PageStoreException;
  String getRoot() throws PageStoreAuthenticationException, PageStoreException;
  long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException;
  SVNNodeKind checkPath(String path, long revision) throws PageStoreAuthenticationException, PageStoreException;

  void getFile(String path, long revision, Map<String, String> properties, OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;

  void ensureDir(String dir, String commitMessage) throws PageStoreException;
  long create(String path, String commitMessage, InputStream content) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException;
  long edit(String path, long baseRevision, String commitMessage, String lockToken, InputStream content) throws PageStoreAuthenticationException, PageStoreException;
  void delete(String path, long baseRevision, String commitMessage, String lockToken) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException;

  void unlock(PageReference ref, String lockToken) throws PageStoreAuthenticationException, PageStoreException;
  void lock(PageReference ref, long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException;
  SVNLock getLock(String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;

}
