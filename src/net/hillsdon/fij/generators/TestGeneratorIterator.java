/**
 * Copyright 2008 Matthew Hillsdon
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
