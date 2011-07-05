package net.hillsdon.reviki.wiki.renderer.creole;

import junit.framework.TestCase;

public class TestLinkParts extends TestCase {

  public void testForcesRefdToHaveContent() throws Exception {
    assertNotNull(new LinkParts(null, null, null).getRefd());
  }
  
}
