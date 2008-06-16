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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageInfo;
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
public class ConfigPageCachingPageStore extends SimpleDelegatingPageStore implements CachingPageStore {

  private static final String CONFIG_PREFIX = "Config";
  private final ConcurrentMap<PageReference, PageInfo> _cache = new ConcurrentHashMap<PageReference, PageInfo>();

  public ConfigPageCachingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    if (revision >= 0 || !isConfigPage(ref.getPath())) {
      return super.get(ref, revision);
    }
    
    // Note the map may be replaced by another thread so we don't reget the page from the cache.
    PageInfo pageInfo = _cache.get(ref);
    if (pageInfo == null || pageInfo.isLocked()) {
      pageInfo = super.get(ref, revision);
      _cache.put(ref, pageInfo);
    }
    return pageInfo;
  }

  @Override
  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    if (isConfigPage(ref.getName())) {
      _cache.remove(ref);
    }
    return super.set(ref, lockToken, baseRevision, content, commitMessage);
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

  /**
   * Exposed for testing.
   * 
   * @param ref A page ref.
   * @return true if we have a cached copy.
   */
  boolean isCached(final PageReference ref) {
    return _cache.containsKey(ref);
  }

}
