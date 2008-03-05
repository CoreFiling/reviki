package net.hillsdon.fij.generators;

import static net.hillsdon.fij.core.Functional.iter;
import junit.framework.TestCase;

public class TestIteratorGenerator extends TestCase {

  public void test() throws Exception {
    IteratorGenerator<String> generator = new IteratorGenerator<String>(iter("one", "two"));
    assertEquals("one", generator.next());
    assertEquals("two", generator.next());
    for (int i = 0; i < 2; ++i) {
      try {
        generator.next();
        fail();
      }
      catch (StopIteration ex) {
        // Expected.
      }
    }
  }
  
}
