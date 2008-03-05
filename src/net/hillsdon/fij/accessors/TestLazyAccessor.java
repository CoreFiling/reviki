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
package net.hillsdon.fij.accessors;

import junit.framework.TestCase;
import net.hillsdon.fij.core.Factory;

public class TestLazyAccessor extends TestCase {

  public void testStraightForwardUse() {
    LazyAccessor<String> accessor = new LazyAccessor<String>(new Factory<String>() {
      public String newInstance() {
        return new String("foo");
      }
    });
    assertSame(accessor.get(), accessor.get());
    assertEquals("foo", accessor.get());
  }
  
  public void testFactoryCanReturnNullWhichWeCache() {
    final int[] calls = {0};
    LazyAccessor<String> accessor = new LazyAccessor<String>(new Factory<String>() {
      public String newInstance() {
        calls[0]++;
        return null;
      }
    });
    
    assertNull(accessor.get());
    assertEquals(1, calls[0]);
    assertNull(accessor.get());
    assertEquals(1, calls[0]);
  }
  
}
