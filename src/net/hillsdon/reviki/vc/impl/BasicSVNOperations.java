/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.vc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import net.hillsdon.reviki.vc.AlreadyLockedException;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;

/**
 * The low-level SVN operations in terms of our classes where useful.
 * 
 * This interface insulates the {@link SVNPageStore} from the {@link SVNRepository}
 * to enable testing of the logic in the page store.
 * 
 * In order to combine multiple operations {@link #execute(SVNAction)} has now
 * been exposed so it may make sense to change the layering here.
 * 
 * @author mth
 */
public interface BasicSVNOperations {

  /**
   * Returns the most recent changes first.
   */
  List<ChangeInfo> log(String path, long limit, boolean pathOnly, boolean stopOnCopy, long startRevision, long endRevision) throws PageStoreAuthenticationException, PageStoreException;

  String getRoot() throws PageStoreAuthenticationException, PageStoreException;
  long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException;
  SVNNodeKind checkPath(String path, long revision) throws PageStoreAuthenticationException, PageStoreException;

  void getFile(String path, long revision, Map<String, String> properties, OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;


  <T> T execute(SVNAction<T> action) throws PageStoreException, PageStoreAuthenticationException;

  /**
   * Caller must openDir.
   */
  void create(ISVNEditor commitEditor, String path, InputStream content) throws SVNException, IOException;
  /**
   * Caller must openDir.
   */
  void edit(ISVNEditor commitEditor, String path, long baseRevision, InputStream content) throws SVNException;
  /**
   * Caller must openDir.
   */
  void delete(ISVNEditor commitEditor, String path, long baseRevision) throws SVNException;
  /**
   * Caller must closeDir afterwards.
   */
  void createDirectory(ISVNEditor commitEditor, String dir) throws SVNException;
  
  /**
   * Currently does open/closeDir.
   */
  void copy(ISVNEditor commitEditor, String fromPath, long fromRevision, String toPath) throws SVNException;
  /**
   * Currently does open/closeDir.
   */
  void moveFile(ISVNEditor commitEditor, String fromPath, long baseRevision, String toPath) throws SVNException;
  /**
   * Currently does open/closeDir.
   */
  void moveDir(ISVNEditor commitEditor, String fromPath, long baseRevision, String toPath) throws SVNException;

  void unlock(PageReference ref, String lockToken) throws PageStoreAuthenticationException, PageStoreException;
  void lock(PageReference ref, long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException;
  SVNLock getLock(String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException;

}
