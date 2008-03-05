package net.hillsdon.svnwiki.web;

import junit.framework.TestCase;

public class TestConsumedPath extends TestCase {

  public void testNextConsumesPath() {
    ConsumedPath path = new ConsumedPath("http://www.example.com/context/some/path?query=moltue", "http://www.example.com/context");
    assertEquals("some", path.next());
    assertTrue(path.hasNext());
    assertEquals("path", path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
  }
  
}
