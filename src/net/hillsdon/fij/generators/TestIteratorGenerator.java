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
