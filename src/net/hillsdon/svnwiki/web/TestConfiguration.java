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
  
//// This touches the file system but is nonetheless useful from time to time.
//  public void testLoadSave() {
//    Configuration c1 = new Configuration();
//    c1.setUrl("http://localhost/svn/wiki");
//    c1.save();
//
//    Configuration c2 = new Configuration();
//    c2.load();
//    assertEquals("http://localhost/svn/wiki", c2.getUrl().toDecodedString());
//  }
  
}
