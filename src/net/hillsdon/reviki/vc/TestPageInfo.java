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

public class TestPageInfo extends TestCase {

  public void testRevisionNameAndIsNew() {
    PageInfo uncommitted = new PageInfo("name", "content", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
    assertEquals("New", uncommitted.getRevisionName());
    assertTrue(uncommitted.isNew());
    PageInfo committed = new PageInfo("name", "content", 5, 2, null, null, null, null);
    assertEquals("r2", committed.getRevisionName());
    assertFalse(committed.isNew());
  }
  
}
