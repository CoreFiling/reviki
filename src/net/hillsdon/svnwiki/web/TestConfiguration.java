package net.hillsdon.svnwiki.web;

import junit.framework.TestCase;

public class TestConfiguration extends TestCase {

  public void testThrowsIllegalArgumentExceptionOnRubbishInput() {
    Configuration configuration = new Configuration();
    try {
      configuration.setUrl("foo bar");
      fail();
    }
    catch (IllegalArgumentException expected) {
    }
  }
  
}
