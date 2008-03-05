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
