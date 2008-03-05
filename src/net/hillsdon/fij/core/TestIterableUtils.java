package net.hillsdon.fij.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.IterableUtils.reversed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class TestIterableUtils extends TestCase {

  public void testReversed() {
    List<Integer> data = new ArrayList<Integer>(asList(1, 2, 3));
    assertEquals(asList(3, 2, 1), list(reversed(data)));
    assertEquals(emptyList(), list(reversed(emptyList())));
    
    Iterator<Integer> iter = reversed(data).iterator();
    iter.next();
    iter.next();
    iter.remove();
    iter.next();
    assertFalse(iter.hasNext());
    assertEquals(asList(1, 3), data);
  }
  
}
