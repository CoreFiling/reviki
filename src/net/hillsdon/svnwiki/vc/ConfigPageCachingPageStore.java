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

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The configuration pages are often accessed, e.g. ConfigSideBar,
 * ConfigInterWikiLinks.  We cache them, forgoing instant response
 * to external commits.
 * 
 * @author mth
 */
public class ConfigPageCachingPageStore extends SimpleDelegatingPageStore {

  private static final String CONFIG_PREFIX = "Config";
  private Map<PageReference, PageInfo> _cache = new LinkedHashMap<PageReference, PageInfo>();

  public ConfigPageCachingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    if (revision == -1 && _cache.containsKey(ref)) {
      return _cache.get(ref);
    }
    PageInfo pageInfo = super.get(ref, revision);
    if (isConfigPage(ref.getPath())) {
      _cache.put(ref, pageInfo);
    }
    return pageInfo;
  }

  @Override
  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    if (isConfigPage(ref.getPath())) {
      _cache = new LinkedHashMap<PageReference, PageInfo>();
    }
    return super.set(ref, lockToken, baseRevision, content, commitMessage);
  }

  static boolean isConfigPage(final String pageName) {
    return pageName.startsWith(CONFIG_PREFIX) 
           && pageName.length() > CONFIG_PREFIX.length()
           && Character.isUpperCase(pageName.charAt(CONFIG_PREFIX.length()));
  }
  
  public PageStore getUnderlying() {
    return getDelegate();
  }

}
