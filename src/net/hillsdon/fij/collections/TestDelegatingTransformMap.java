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
