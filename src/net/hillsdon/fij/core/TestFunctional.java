/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.fij.core;

import static java.util.Arrays.asList;
import static net.hillsdon.fij.core.Functional.filter;
import static net.hillsdon.fij.core.Functional.iter;
import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.core.Functional.set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

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
      public Integer transform(final Integer in) {
        return in + 1;
      }
    })));
  }

  public void testFilter() {
    assertEquals(asList(1, 3), list(filter(asList(1, 2, 3), new InversePredicate<Integer>(new Predicate<Integer>() {
      public Boolean transform(final Integer in) {
        return in % 2 == 0;
      }
    }))));
  }
  
  public void testCantRemoveFromFilterIterator() {
    Iterator<Object> iterator = filter(set(""), new Predicate<Object>() {
      public Boolean transform(final Object in) {
        return true;
      }
    });
    iterator.next();
    try {
      iterator.remove();
      fail();
    }
    catch (UnsupportedOperationException ex) {
    }
  }

  public void testCantRemoveFromMapIterator() {
    Iterator<String> iterator = map(set(""), new Transform<String, String>() {
      public String transform(final String in) {
        return "delta " + in;
      }
    });
    iterator.next();
    try {
      iterator.remove();
      fail();
    }
    catch (UnsupportedOperationException ex) {
    }
  }

  public void testSet() {
    Set<String> set = set("one", "one", "two");
    assertTrue(set.contains("one"));
    assertTrue(set.contains("two"));
    assertEquals(2, set.size());
  }
  
  public void testSetOfLinkedHashSetIsSame() {
    LinkedHashSet<String> set = new LinkedHashSet<String>();
    set(set);
    assertSame(set, set(set));
  }
  
  public void testSetOfOtherSetIsLinkedHashSet() {
    assertTrue(set(new HashSet<String>()) instanceof LinkedHashSet);
  }
  
  public void testListOfListIsSame() {
    ArrayList<String> list = new ArrayList<String>();
    assertSame(list, list(list));
  }
  
}
