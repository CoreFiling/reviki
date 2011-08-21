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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.hillsdon.reviki.vc.AttachmentHistory;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreAuthenticationException;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.PageStoreInvalidException;



/**
 * Delegates all functionality to given delegate.  Subclass to alter behaviour.
 *
 * @author mth
 */
public abstract class AbstractDelegatingPageStore extends AbstractPageStore {

  /**
   * @return The delegate to use.  This is called for each delegation.
   */
  protected abstract PageStore getDelegate();

  /**
   * With bonus null check.
   *
   * @return The page store.
   */
  private PageStore getDelegateInternal() {
    PageStore delegate = getDelegate();
    if (delegate == null) {
      throw new IllegalStateException("No delegate available!");
    }
    return delegate;
  }

  public VersionedPageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    return getDelegateInternal().get(ref, revision);
  }

  public Set<PageReference> list() throws PageStoreException {
    return getDelegateInternal().list();
  }

  public List<ChangeInfo> recentChanges(final long limit) throws PageStoreException {
    return getDelegateInternal().recentChanges(limit);
  }

  public long set(final PageInfo page, final String lockToken, final long baseRevision, final String commitMessage) throws InterveningCommitException, PageStoreException {
    return getDelegateInternal().set(page, lockToken, baseRevision, commitMessage);
  }

  public long deleteAttachment(PageReference ref, String attachmentName, long baseRevision, String commitMessage) throws PageStoreAuthenticationException, PageStoreException {
    return getDelegateInternal().deleteAttachment(ref, attachmentName, baseRevision, commitMessage);
  }

  public VersionedPageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return getDelegateInternal().tryToLock(ref);
  }

  public void unlock(final PageReference ref, final String lockToken) throws PageStoreException {
    getDelegateInternal().unlock(ref, lockToken);
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return getDelegateInternal().history(ref);
  }

  public void attach(final PageReference page, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    getDelegateInternal().attach(page, storeName, baseRevision, in, commitMessage);
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return getDelegateInternal().attachments(ref);
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    getDelegateInternal().attachment(ref, attachment, revision, sink);
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) throws PageStoreException {
    return getDelegateInternal().getChangedBetween(start, end);
  }

  public long getLatestRevision() throws PageStoreAuthenticationException, PageStoreException {
    return getDelegateInternal().getLatestRevision();
  }

  public long copy(final PageReference from, final long fromRevision, final PageReference to, final String commitMessage) throws PageStoreException {
    return getDelegateInternal().copy(from, fromRevision, to, commitMessage);
  }

  public long rename(final PageReference from, final PageReference to, final long baseRevision, final String commitMessage) throws InterveningCommitException, PageStoreException {
    return getDelegateInternal().rename(from, to, baseRevision, commitMessage);
  }

  public void assertValid() throws PageStoreInvalidException, PageStoreAuthenticationException {
    getDelegateInternal().assertValid();
  }

  public String getWiki() throws PageStoreException {
    return getDelegateInternal().getWiki();
  }
}
