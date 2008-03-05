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
package net.hillsdon.svnwiki.web.common;

import junit.framework.TestCase;

public class TestConsumedPath extends TestCase {

  public void testNextConsumesPath() {
    ConsumedPath path = new ConsumedPath("http://www.example.com/context/my%20very%20own/path?query=moltue", "http://www.example.com/context");
    assertEquals("my very own", path.next());
    assertTrue(path.hasNext());
    assertEquals("path", path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
    assertFalse(path.hasNext());
    assertNull(path.next());
  }
  
}
