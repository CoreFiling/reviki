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
package net.hillsdon.svnwiki.web.vcintegration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimpleDelegatingPageStore;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns default FrontPage etc if there isn't one in the repository.
 * 
 * @author mth
 */
public class SpecialPagePopulatingPageStore extends SimpleDelegatingPageStore {

  private static final Log LOG = LogFactory.getLog(SpecialPagePopulatingPageStore.class);
  private static final Collection<PageReference> SPECIAL_PAGES_WITH_CONTENT = new LinkedHashSet<PageReference>(Arrays.asList(
      new PageReference("FrontPage"),
      new PageReference("FindPage"),
      new PageReference("ConfigCss"),
      new PageReference("ConfigSideBar"),
      new PageReference("ConfigInterWikiLinks")
   )); 
  private static final Collection<PageReference> SPECIAL_PAGES_WITHOUT_CONTENT = Arrays.asList(
      new PageReference("AllPages"),
      new PageReference("ConfigSvnLocation"),
      new PageReference("OrphanedPages"),
      new PageReference("RecentChanges") 
    );
  
  public SpecialPagePopulatingPageStore(final PageStore delegate) {
    super(delegate);
  }

  @Override
  public Collection<PageReference> list() throws PageStoreException {
    Collection<PageReference> list = super.list();
    list.addAll(SPECIAL_PAGES_WITH_CONTENT);
    list.addAll(SPECIAL_PAGES_WITHOUT_CONTENT);
    return list;
  }
  
  private PageInfo withContentIfSpecialAndNew(final PageReference ref, PageInfo page) {
    try {
      if (page.isNew() && SPECIAL_PAGES_WITH_CONTENT.contains(ref)) {
        String text = IOUtils.toString(getClass().getResourceAsStream("prepopulated/" + page.getPath()), "UTF-8");
        page = new PageInfo(page.getPath(), text, PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, page.getLastChangedUser(), page.getLastChangedDate(), page.getLockedBy(), page.getLockToken());
      }
    }
    catch (IOException ex) {
      LOG.warn("Error retrieving prepopulated page.", ex);
    }
    return page;
  }

  public PageInfo get(final PageReference ref, final long revision) throws PageStoreException {
    PageInfo page = super.get(ref, revision);
    page = withContentIfSpecialAndNew(ref, page);
    return page;
  }

  @Override
  public PageInfo tryToLock(final PageReference ref) throws PageStoreException {
    PageInfo page = super.tryToLock(ref);
    page = withContentIfSpecialAndNew(ref, page);
    return page;
  }
  
}
