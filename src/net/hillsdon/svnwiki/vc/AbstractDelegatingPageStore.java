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
import java.util.Collection;
import java.util.List;
import java.util.Set;



/**
 * Delegates all functionality to given delegate.  Subclass to alter behaviour. 
 * 
 * @author mth
 */
public abstract class AbstractDelegatingPageStore implements PageStore {

  /**
   * @return The delegate to use.  This is called for each delegation.
   */
  protected abstract PageStore getDelegate();

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return getDelegate().get(ref, revision);
  }

  public Set<PageReference> list() throws PageStoreException {
    return getDelegate().list();
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return getDelegate().recentChanges(limit);
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    return getDelegate().set(ref, lockToken, baseRevision, content, commitMessage);
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return getDelegate().tryToLock(ref);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreException {
    getDelegate().unlock(ref, lockToken);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return getDelegate().history(ref);
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    getDelegate().attach(ref, storeName, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return getDelegate().attachments(ref);
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    getDelegate().attachment(ref, attachment, revision, sink);
  }

  public Collection<PageReference> getChangedBetween(final long start, long end) throws PageStoreException {
    return getDelegate().getChangedBetween(start, end);
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return getDelegate().getLatestRevision();
  }

}
