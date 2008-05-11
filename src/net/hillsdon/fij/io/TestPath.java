package net.hillsdon.fij.io;

import java.io.File;

import junit.framework.TestCase;

public class TestPath extends TestCase {

  public void test() {
    assertEquals("", Path.join());
    assertEquals("foo", Path.join("foo"));
    assertEquals("foo" + File.separator + "bar", Path.join("foo", "bar"));
  }
  
}
