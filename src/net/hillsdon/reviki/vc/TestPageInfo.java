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
package net.hillsdon.reviki.vc;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.impl.SVNPathLinkTarget;
import net.hillsdon.reviki.vc.impl.VersionedPageInfoImpl;
import net.hillsdon.reviki.wiki.renderer.creole.SimplePageLinkTarget;

public class TestPageInfo extends TestCase {

  public void testRevisionNameAndIsNew() {
    VersionedPageInfo uncommitted = new VersionedPageInfoImpl("wiki", "name", "content", VersionedPageInfo.UNCOMMITTED, VersionedPageInfo.UNCOMMITTED, null, null, null, null, null);
    assertEquals("New", uncommitted.getRevisionName());
    assertTrue(uncommitted.isNewPage());
    VersionedPageInfo committed = new VersionedPageInfoImpl("wiki", "name", "content", 5, 2, null, null, null, null, null);
    assertEquals("r2", committed.getRevisionName());
    assertFalse(committed.isNewPage());
    assertFalse(committed.isRenamed());
  }
  
  public void testRenamedPageNameIsRenamed() {
    VersionedPageInfo renamed = new VersionedPageInfoImpl("wiki", "name", "content", VersionedPageInfo.RENAMED, 6, null, null, null, null, null, new SimplePageLinkTarget(null, "renamed", null, null));
    assertEquals(renamed.getRenamedPageName(), "renamed");
    assertTrue(renamed.isNewPage());
    assertTrue(renamed.isRenamed());
    assertTrue(renamed.isRenamedInThisWiki());
  }
  
  /** Can we react appropriately to renames into other wikis https://jira.int.corefiling.com/browse/REVIKI-552 */
  public void testRenamedPageNameIsMovedToOtherWiki() {
    VersionedPageInfo renamed = new VersionedPageInfoImpl("wiki", "name", "content", VersionedPageInfo.RENAMED, 6, null, null, null, null, null, new SVNPathLinkTarget("http://svn.example.com/svn/", "/some/path/renamed"));
    assertEquals(renamed.getRenamedPageName(), "renamed");
    assertTrue(renamed.isNewPage());
    assertTrue(renamed.isRenamed());
    assertFalse(renamed.isRenamedInThisWiki());
  }
}
