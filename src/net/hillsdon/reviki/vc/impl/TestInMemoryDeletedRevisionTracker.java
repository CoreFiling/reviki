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

import java.util.Collections;
import java.util.Date;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeType;
import net.hillsdon.reviki.vc.StoreKind;

import static java.util.Collections.emptySet;

import static net.hillsdon.fij.core.Functional.set;

/**
 * Test for {@link InMemoryDeletedRevisionTracker}.
 * 
 * @author mth
 */
public class TestInMemoryDeletedRevisionTracker extends TestCase {

  public void test() throws Exception {
    ChangeInfo add = new ChangeInfo("PageOne", "PageOne", "user", new Date(), 1, "Initial", StoreKind.PAGE, ChangeType.ADDED, null, -1);
    ChangeInfo delete = new ChangeInfo("PageOne", "PageOne", "user", new Date(), 1, "Delete", StoreKind.PAGE, ChangeType.DELETED, null, -1);
   
    InMemoryDeletedRevisionTracker tracker = new InMemoryDeletedRevisionTracker();
    assertEquals(emptySet(), tracker.currentExistingEntries());
    assertEquals(0L, tracker.getHighestSyncedRevision());
    tracker.handleChanges(1, Collections.singletonList(add));
    assertEquals(set("PageOne"), tracker.currentExistingEntries());
    tracker.handleChanges(1, Collections.singletonList(delete));
    assertEquals(emptySet(), tracker.currentExistingEntries());
  }
  
}
