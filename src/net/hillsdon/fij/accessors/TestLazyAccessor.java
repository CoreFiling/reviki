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
