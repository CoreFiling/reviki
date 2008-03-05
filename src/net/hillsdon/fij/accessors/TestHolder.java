package net.hillsdon.fij.accessors;

import junit.framework.TestCase;

public class TestHolder extends TestCase {

  public void testNullConstructionPermitted() {
    Holder<String> h = new Holder<String>(null);
    assertNull(h.get());
  }
  
  public void testIsMutable() {
    final String foo = "foo";
    final String bar = "bar";

    Holder<String> h = new Holder<String>(bar);
    assertSame(bar, h.get());
    h.set(foo);
    assertSame(foo, h.get());
  }
  
}
