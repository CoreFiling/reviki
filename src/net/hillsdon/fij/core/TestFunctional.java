package net.hillsdon.fij.core;

import static java.util.Arrays.asList;
import static net.hillsdon.fij.core.Functional.filter;
import static net.hillsdon.fij.core.Functional.iter;
import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class TestFunctional extends TestCase {

  public void testIter() {
    assertEquals(asList("1", "2", "3"), list(iter("1", "2", "3")));
    assertFalse(iter().hasNext());
    try {
      iter().next();
      fail();
    }
    catch (NoSuchElementException ex) {
      // Required by interface.
    }
  }

  public void testMap() {
    assertEquals(asList(2, 3, 4), list(map(asList(1, 2, 3), new Transform<Integer, Integer>() {
      public Integer transform(Integer in) {
        return in + 1;
      }
    })));
  }

  public void testFilter() {
    assertEquals(asList(1, 3), list(filter(asList(1, 2, 3), new InversePredicate<Integer>(new Predicate<Integer>() {
      public Boolean transform(Integer in) {
        return in % 2 == 0;
      }
    }))));
  }

  
}
