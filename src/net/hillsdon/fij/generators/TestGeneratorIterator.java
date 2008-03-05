package net.hillsdon.fij.generators;

import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.generators.GeneratorUtil.emptyGenerator;

import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class TestGeneratorIterator extends TestCase {

  public void testNaturalNumbers() {
    Generator<Integer> generator = new Generator<Integer>() {
      int i = 0;
      public Integer next() throws StopIteration {
        if (i < 100) {
          return i++;
        }
        throw new StopIteration();
      }
    };
    List<Integer> results = list(new GeneratorIterator<Integer>(generator));
    assertEquals(100, results.size());
    assertEquals(new Integer(0), results.get(0));
    assertEquals(new Integer(99), results.get(99));
  }
  
  public void testEmpty() {
    GeneratorIterator<Object> iterator = new GeneratorIterator<Object>(emptyGenerator());
    try {
      iterator.next();
      fail();
    }
    catch (NoSuchElementException ex) {
      // Required by iterface.
    }
    assertFalse(iterator.hasNext());
  }
  
}
