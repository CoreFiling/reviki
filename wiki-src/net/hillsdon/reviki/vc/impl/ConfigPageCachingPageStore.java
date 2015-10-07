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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeSubscriber;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.VersionedPageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;


/**
 * The configuration pages are often accessed, e.g. ConfigSideBar,
 * ConfigInterWikiLinks.  We cache them, forgoing instant response
 * to external commits.
 *
 * @author mth
 */
public class ConfigPageCachingPageStore extends SimpleDelegatingPageStore implements CachingPageStore, ChangeSubscriber {
  private static final Log LOG = LogFactory.getLog(ConfigPageCachingPageStore.class);
  private long _lowestUnsyncedRevision = Integer.MAX_VALUE; // When the cache is empty nothing is unsynced

  private static final String CONFIG_PREFIX = "Config";
  private final ConcurrentMap<PageReference, VersionedPageInfo> _cache = new ConcurrentHashMap<PageReference, VersionedPageInfo>();

  public ConfigPageCachingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public VersionedPageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    if (revision >= 0 || !isConfigPage(ref.getPath())) {
      return super.get(ref, revision);
    }

    VersionedPageInfo pageInfo = _cache.get(ref);
    if (pageInfo == null) {
      pageInfo = super.get(ref, revision);
      // NB. revision is one of -1, -2, -3, -4. See VersionedPageInfoImpl
      synchronized (this) {
        long pageRev = pageInfo.getRevision();
        LOG.debug("Caching: " + ref.getPath() + " Revision: " + Long.toString(pageRev));
        _cache.put(ref, pageInfo);
        // Do not record reviki internal revisions (one of -1. -2. -3. -4 see VersionedPageInfoImpl).
        if (pageRev >= 0 && pageRev <= _lowestUnsyncedRevision) {
          _lowestUnsyncedRevision = pageRev + 1;
        }
      }
    }
    return pageInfo;
  }

  @Override
  public long set(final PageInfo page, final String lockToken, final long baseRevision, final String commitMessage) throws InterveningCommitException, PageStoreException {
    if (isConfigPage(page.getName())) {
      _cache.remove(page);
    }
    return super.set(page, lockToken, baseRevision, commitMessage);
  }

  static boolean isConfigPage(final String pageName) {
    return pageName.startsWith(CONFIG_PREFIX)
           && pageName.length() > CONFIG_PREFIX.length()
           && Character.isUpperCase(pageName.charAt(CONFIG_PREFIX.length()));
  }

  /**
   * @return The underlying page store that non-caching access is delegated to.
   */
  public PageStore getUnderlying() {
    return getDelegate();
  }

  public void expire(final PageReference ref) {
    _cache.remove(ref);
  }

  /**
   * Exposed for testing.
   *
   * @param ref A page ref.
   * @return true if we have a cached copy.
   */
  boolean isCached(final PageReference ref) {
    return _cache.containsKey(ref);
  }

  @Override
  public long getHighestSyncedRevision() throws IOException {
    return _lowestUnsyncedRevision - 1;
  }

  @Override
  public synchronized void handleChanges(long upto, List<ChangeInfo> chronological) throws PageStoreException, IOException {
    for (ChangeInfo change: chronological) {
      PageReference pr = new PageReferenceImpl(change.getPage());
      expire(pr);
      _lowestUnsyncedRevision = change.getRevision() + 1;
    }
  }
}
