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

/**
 * Delegates to {@link #getDelegate()}.
 * 
 * @author mth
 */
public abstract class DelegatingBasicSVNOperations implements BasicSVNOperations {

  public SVNNodeKind checkPath(final String path, final long revision) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().checkPath(path, revision);
  }

  public void copy(ISVNEditor commitEditor, final String fromPath, final long fromRevision, final String toPath) throws SVNException {
    getDelegate().copy(commitEditor, fromPath, fromRevision, toPath);
  }

  public void create(ISVNEditor commitEditor, final String path, final InputStream content) throws SVNException, IOException {
    getDelegate().create(commitEditor, path, content);
  }

  public void delete(ISVNEditor commitEditor, final String path, final long baseRevision) throws SVNException {
    getDelegate().delete(commitEditor, path, baseRevision);
  }

  public void edit(ISVNEditor commitEditor, final String path, final long baseRevision, final InputStream content) throws SVNException {
    getDelegate().edit(commitEditor, path, baseRevision, content);
  }

  public void createDirectory(ISVNEditor commitEditor, final String dir) throws SVNException {
    getDelegate().createDirectory(commitEditor, dir);
  }
  
  public void moveDir(ISVNEditor commitEditor, String fromPath, long baseRevision, String toPath) throws SVNException {
    getDelegate().moveDir(commitEditor, fromPath, baseRevision, toPath);
  }
  
  public void moveFile(ISVNEditor commitEditor, String fromPath, long baseRevision, String toPath) throws SVNException {
    getDelegate().moveFile(commitEditor, fromPath, baseRevision, toPath);
  }

  public <T> T execute(final SVNAction<T> action) throws PageStoreException, PageStoreAuthenticationException {
    return getDelegate().execute(action);
  }

  public void getFile(final String path, final long revision, final Map<String, String> properties, final OutputStream out) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    getDelegate().getFile(path, revision, properties, out);
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().getLatestRevision();
  }

  public SVNLock getLock(final String path) throws NotFoundException, PageStoreAuthenticationException, PageStoreException {
    return getDelegate().getLock(path);
  }

  public String getRoot() throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().getRoot();
  }

  public void lock(final PageReference ref, final long revision) throws AlreadyLockedException, PageStoreAuthenticationException, PageStoreException {
    getDelegate().lock(ref, revision);
  }

  public List<ChangeInfo> log(final String path, final long limit, boolean pathOnly, final boolean stopOnCopy, final long startRevision, final long endRevision) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().log(path, limit, pathOnly, stopOnCopy, startRevision, endRevision);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreAuthenticationException, PageStoreException {
    getDelegate().unlock(ref, lockToken);
  }

  protected abstract BasicSVNOperations getDelegate();

}
