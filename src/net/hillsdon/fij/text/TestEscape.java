package net.hillsdon.fij.text;

import junit.framework.TestCase;

public class TestEscape extends TestCase {

  public void testNullEscapesToEmptyStringForConvenience() {
    assertEquals("", Escape.html(null));
  }
  
}
