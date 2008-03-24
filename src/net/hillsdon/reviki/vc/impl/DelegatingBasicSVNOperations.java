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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.hillsdon.reviki.vc.AlreadyLockedException;
import net.hillsdon.reviki.vc.BasicSVNOperations;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.NotFoundException;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;

import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;

/**
 * Delegates to {@link #getDelegate()}.
 * 
 * @author mth
 */
public abstract class DelegatingBasicSVNOperations implements BasicSVNOperations {

  public SVNNodeKind checkPath(final String path, final long revision) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().checkPath(path, revision);
  }

  public long copy(final String fromPath, final long fromRevision, final String toPath, final String commitMessage) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException {
    return getDelegate().copy(fromPath, fromRevision, toPath, commitMessage);
  }

  public long create(final String path, final String commitMessage, final InputStream content) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException {
    return getDelegate().create(path, commitMessage, content);
  }

  public long delete(final String path, final long baseRevision, final String commitMessage, final String lockToken) throws InterveningCommitException, PageStoreAuthenticationException, PageStoreException {
    return getDelegate().delete(path, baseRevision, commitMessage, lockToken);
  }

  public long edit(final String path, final long baseRevision, final String commitMessage, final String lockToken, final InputStream content) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().edit(path, baseRevision, commitMessage, lockToken, content);
  }

  public void ensureDir(final String dir, final String commitMessage) throws PageStoreException {
    getDelegate().ensureDir(dir, commitMessage);
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

  public Collection<String> listFiles(final String dir) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().listFiles(dir);
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
