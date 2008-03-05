/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  List<ChangeInfo> log(String path, long limit, boolean pathOnly, boolean stopOnCopy, long startRevision, long endRevision) throws PageStoreAuthenticationException, PageStoreException;

  Collection<String> listFiles(String dir) throws PageStoreAuthenticationException, PageStoreException;
  String getRoot() throws PageStoreAuthenticationException, PageStoreException;
  long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException;
  SVNNodeKind checkPath(String path, long revision) throws PageStoreAuthenticationException, PageStoreException;

  void getFile(String path, long revision, Map<String, String> properties, OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;

  void ensureDir(String dir, String commitMessage) throws PageStoreException;
  long create(String path, String commitMessage, InputStream content) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException;
  long edit(String path, long baseRevision, String commitMessage, String lockToken, InputStream content) throws PageStoreAuthenticationException, PageStoreException;
  long delete(String path, long baseRevision, String commitMessage, String lockToken) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException;
  long copy(String fromPath, long fromRevision, String toPath, String commitMessage) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException;
  long rename(String fromPath, String toPath, long baseRevision, String commitMessage) throws PageStoreAuthenticationException, PageStoreException;

  void unlock(PageReference ref, String lockToken) throws PageStoreAuthenticationException, PageStoreException;
  void lock(PageReference ref, long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException;
  SVNLock getLock(String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;

}
