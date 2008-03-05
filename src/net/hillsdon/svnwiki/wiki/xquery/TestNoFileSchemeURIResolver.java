package net.hillsdon.svnwiki.wiki.xquery;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

public class TestNoFileSchemeURIResolver extends TestCase {

  private static final String[][] INVALID = new String[][] {
      new String[] {"file:/etc/passwd", null},
      new String[] {"", "file:/etc/passwd"},
      new String[] {"etc/password", "file:/"},
    };

  private static final String[][] VALID = new String[][] {
    new String[] {"http://www.example.com/etc/passwd", null},
    new String[] {"", "http://www.example.com/etc/passwd"},
    new String[] {"etc/password", "http://www.example.com/"},
  };
  
  public void testInvalidThrowsException() {
    for (String[] parts : INVALID) {
      try {
        new NoFileSchemeURIResolver().resolve(parts[0], parts[1]);
        fail();
      }
      catch (TransformerException expected) {
      }
    }
  }

  public void testValidReturnsNull() throws Exception {
    for (String[] parts : VALID) {
      new NoFileSchemeURIResolver().resolve(parts[0], parts[1]);
    }
  }
  
}
