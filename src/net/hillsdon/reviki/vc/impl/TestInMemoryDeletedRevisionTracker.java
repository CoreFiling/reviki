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
