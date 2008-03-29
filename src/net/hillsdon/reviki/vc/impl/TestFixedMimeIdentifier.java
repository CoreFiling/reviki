package net.hillsdon.reviki.vc.impl;

import junit.framework.TestCase;

public class TestFixedMimeIdentifier extends TestCase {

  public void test() {
    FixedMimeIdentifier mimeIdentifier = new FixedMimeIdentifier();
    assertTrue(mimeIdentifier.isImage("foo.png"));
    assertFalse(mimeIdentifier.isImage("foo.doc"));
    assertFalse(mimeIdentifier.isImage(".doc"));
    assertFalse(mimeIdentifier.isImage("."));
    assertFalse(mimeIdentifier.isImage("foo."));
  }
  
}
