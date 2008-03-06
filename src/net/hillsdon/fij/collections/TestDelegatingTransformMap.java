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
package net.hillsdon.fij.collections;

import junit.framework.TestCase;
import net.hillsdon.fij.core.Transform;

public class TestDelegatingTransformMap extends TestCase {

  private Transform<Person, String> TO_NAME = new Transform<Person, String>() {
    public String transform(final Person in) {
      return in.getName();
    }
  };

  private static class Person {
    private final String _name;
    public Person(final String name) {
      _name = name;
    }
    public String getName() {
      return _name;
    }
  }

  public void test() {
    Person ted1 = new Person("Ted");
    Person ted2 = new Person("Ted");
    
    TransformMap<String,Person> map = new DelegatingTransformMap<String, Person>(TO_NAME);
    map.put(ted1);
    assertEquals(ted1, map.get("Ted"));
    
    map.put(ted2);
    assertEquals(1, map.size());
    
    Person bob = new Person("Bob");
    map.values().add(bob);
    assertEquals(bob, map.get("Bob"));
  }

}
