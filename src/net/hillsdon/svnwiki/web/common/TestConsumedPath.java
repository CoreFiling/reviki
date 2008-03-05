package net.hillsdon.svnwiki.web.common;

import junit.framework.TestCase;

public class TestConsumedPath extends TestCase {

  public void testNextConsumesPath() {
    ConsumedPath path = new ConsumedPath("http://www.example.com/context/my%20very%20own/path?query=moltue", "http://www.example.com/context");
    assertEquals("my very own", path.next());
    assertTrue(path.hasNext());
    assertEquals("path", path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
  }
  
}
