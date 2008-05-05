package net.hillsdon.reviki.vc.impl;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageReference;

public class TestPageReferenceImpl extends TestCase {

  private static final String PATH = "foo/bar/BlortPage";
  private static final PageReference REFERENCE = new PageReferenceImpl(PATH);
  
  public void testPathIsAsGiven() {
    assertEquals(PATH, REFERENCE.getPath());
  }
  
  public void testNameBasedOnPath() {
    assertEquals("BlortPage", REFERENCE.getName());
  }
  
  public void testTitleBasedOnName() {
    assertEquals("Blort Page", REFERENCE.getTitle());
  }
  
}
