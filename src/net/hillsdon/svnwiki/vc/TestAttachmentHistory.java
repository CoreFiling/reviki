package net.hillsdon.svnwiki.vc;

import java.util.Collections;
import java.util.Date;

import junit.framework.TestCase;

public class TestAttachmentHistory extends TestCase {

  public void testGetPreviousVersions() {
    AttachmentHistory history = new AttachmentHistory();
    ChangeInfo firstCommit = new ChangeInfo("FooPage", "FooPage", "mth", new Date(), 1, "Added.", StoreKind.ATTACHMENT);
    ChangeInfo secondCommit = new ChangeInfo("FooPage", "FooPage", "mth", new Date(), 1, "Latest edit.", StoreKind.ATTACHMENT);
    history.getVersions().add(secondCommit);
    history.getVersions().add(firstCommit);
    assertEquals(secondCommit, history.getLatestVersion());
    assertEquals(Collections.singletonList(firstCommit), history.getPreviousVersions());
  }
  
}
