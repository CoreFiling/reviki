package net.hillsdon.svnwiki.vc;

import junit.framework.TestCase;

public class TestPageInfo extends TestCase {

  public void testRevisionNameAndIsNew() {
    PageInfo uncommitted = new PageInfo("name", "content", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, null, null, null, null);
    assertEquals("New", uncommitted.getRevisionName());
    assertTrue(uncommitted.isNew());
    PageInfo committed = new PageInfo("name", "content", 1, 1, null, null, null, null);
    assertEquals("r1", committed.getRevisionName());
    assertFalse(committed.isNew());
  }
  
}
