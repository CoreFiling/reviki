package net.hillsdon.fij.text;

import static net.hillsdon.fij.core.Functional.iter;
import static net.hillsdon.fij.text.Strings.join;
import junit.framework.TestCase;

public class TestStrings extends TestCase {

  public void testJoin() {
    assertEquals("1, 2, 3", join(iter(1, 2, 3), ", "));
  }
  
}
