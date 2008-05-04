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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.hillsdon.reviki.vc.AttachmentHistory;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ContentTypedSink;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;

/**
 * Partial implementation suitable for tests.
 * 
 * @author mth
 */
public class SimplePageStore implements CachingPageStore {

  private Map<PageReference, PageInfo> _pages = new LinkedHashMap<PageReference, PageInfo>();
  
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    PageInfo page = _pages.get(ref);
    if (page == null) {
      page = new PageInfoImpl(ref.getPath(), "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
      _pages.put(ref, page);
    }
    return page;
  }

  public Set<PageReference> list() throws PageStoreException {
    return new LinkedHashSet<PageReference>(_pages.keySet());
  }

  public List<ChangeInfo> recentChanges(final int limit) throws PageStoreException {
    return Collections.emptyList();
  }

  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws PageStoreException {
    long revision = baseRevision + 1;
    PageInfo page = new PageInfoImpl(ref.getPath(), content, revision, revision, null, null, null, null);
    _pages.put(ref, page);
    return revision;
  }

  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    return get(ref, -1);
  }

  public void unlock(final PageReference ref, final String lockToken) {
  }

  public List<ChangeInfo> history(final PageReference ref) throws PageStoreException {
    return Collections.emptyList();
  }

  public void attach(final PageReference ref, final String storeName, final long baseRevision, final InputStream in, final String commitMessage) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public Collection<AttachmentHistory> attachments(final PageReference ref) throws PageStoreException {
    return Collections.emptySet();
  }

  public void attachment(final PageReference ref, final String attachment, final long revision, final ContentTypedSink sink) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public Collection<PageReference> getChangedBetween(final long start, final long end) {
    throw new UnsupportedOperationException();
  }

  public long getLatestRevision() {
    throw new UnsupportedOperationException();
  }

  public long copy(final PageReference from, final long fromRevision, final PageReference to, final String commitMessage) throws PageStoreException {
    throw new UnsupportedOperationException();
  }

  public long rename(PageReference from, PageReference to, long baseRevision, String commitMessage) throws InterveningCommitException, PageStoreException {
    throw new UnsupportedOperationException();
  }

  public PageStore getUnderlying() {
    return this;
  }
  
}