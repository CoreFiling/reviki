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

import static net.hillsdon.reviki.vc.impl.ConfigPageCachingPageStore.isCacheableConfigPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.VersionedPageInfo;

public class TestConfigPageCachingPageStore extends TestCase {

  public void testIsConfigPage() {
    assertFalse(isCacheableConfigPage("Config"));
    assertTrue(isCacheableConfigPage("ConfigFoo"));
    assertFalse(isCacheableConfigPage("ConfiguringStuff"));
  }

  public void testDoesntCacheOldRevisionsOfConfigPages() throws Exception {
    PageInfo page = new PageInfoImpl(null, "ConfigFoo", "Hey there", Collections.<String, String>emptyMap());
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    store.getUnderlying().set(page, "", 1, "Initial commit");
    assertFalse(store.get(page, 2).isNewPage());
    assertFalse(store.isCached(page));
  }

  public void testLockedConfigPageUsesCache() throws Exception {
    VersionedPageInfo page = new VersionedPageInfoImpl(null, "ConfigFoo", "foo", 1, 1, "pwc", new Date(), null, null, null);
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    store.getUnderlying().set(page, "", 1, "Initial commit");
    store.get(page, -1);

    // Sanity check the page is cached
    assertTrue(store.isCached(page));

    // Lock the page
    VersionedPageInfo lockedPage = new VersionedPageInfoImpl(null, "ConfigFoo", "bar", 2, 2, "pwc", new Date(), "pwc", null, null);

    // Sanity check the page is locked
    assertTrue(lockedPage.isLocked());

    // Store the locked page
    store.getUnderlying().set(lockedPage, "", 2, "Second commit");

    // Sanity check that page and lockedPage have different content
    assertFalse(lockedPage.getContent().equals(page.getContent()));

    // We should get the cached page when fetching the page referenced by
    // the locked page.
    VersionedPageInfo cachedPage = store.get(lockedPage, -1);
    assertTrue(cachedPage.getContent().equals(page.getContent()));

    // Sanity check expiring the page referenced by the lockedPage
    // and retrieving the lockedPage from the store.
    store.expire(lockedPage);
    VersionedPageInfo afterExpire = store.get(lockedPage, -1);
    assertTrue(afterExpire.getContent().equals(lockedPage.getContent()));
  }

  public void testSpecialRevisionIsCached() throws Exception {
    PageInfo page = new PageInfoImpl(null, "ConfigFoo", "Hey there", Collections.<String, String>emptyMap());
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());

    // Cache the page
    VersionedPageInfo uncommittedPage = store.get(page, -1);

    // Sanity check the uncommmitted revision
    assertTrue(uncommittedPage.getRevision() == -2L);

    assertTrue(store.isCached(uncommittedPage));

    // Check that the highestSyncedRevision was not set to the internal revision
    assertTrue(store.getHighestSyncedRevision() > 0);
  }

  public void testChangesExpireCache() throws Exception {
    // Cache a page
    PageInfo page = new PageInfoImpl(null, "ConfigFoo", "Hey there", Collections.<String, String>emptyMap());
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    store.getUnderlying().set(page, "", 1, "Initial commit");
    store.get(page, -1);
    assertTrue(store.isCached(page));

    // Assemble a change for the page
    List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    changes.add(new ChangeInfo(page.getName(), page.getName(), "pwc", new Date(), 3L, "Second revision", null, null, null, -2));

    // Call handleChanges, should expire the cache
    store.handleChanges(3L, changes);
    assertFalse(store.isCached(page));
  }

  public void testChangeToOtherPageDoesNotExpireCache() throws Exception {
    // Cache a page
    PageInfo page = new PageInfoImpl(null, "ConfigFoo", "Hey there", Collections.<String, String>emptyMap());
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    store.getUnderlying().set(page, "", 1, "Initial commit");
    store.get(page, -1);
    assertTrue(store.isCached(page));

    // Assemble a change for the page
    List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    changes.add(new ChangeInfo("ConfigBar", "ConfigBar", "pwc", new Date(), 3L, "Second revision", null, null, null, -2));

    // Call handleChanges, should not expire the cache
    store.handleChanges(3L, changes);
    assertTrue(store.isCached(page));
  }

  public void testChangesUpdateHighestSyncedRevision() throws Exception {
    ConfigPageCachingPageStore store = new ConfigPageCachingPageStore(new SimplePageStore());
    // Assemble a change for the page
    List<ChangeInfo> changes = new ArrayList<ChangeInfo>();
    changes.add(new ChangeInfo("ConfigBar", "ConfigBar", "pwc", new Date(), 3L, "Second revision", null, null, null, -2));
    changes.add(new ChangeInfo("ConfigBar", "ConfigBar", "pwc", new Date(), 4L, "Second revision", null, null, null, -2));
    changes.add(new ChangeInfo("ConfigBar", "ConfigBar", "pwc", new Date(), 43L, "Second revision", null, null, null, -2));

    store.handleChanges(43L, changes);

    assertTrue(store.getHighestSyncedRevision() == 43L);
  }
}
