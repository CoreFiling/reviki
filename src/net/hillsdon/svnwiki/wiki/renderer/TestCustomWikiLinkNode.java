package net.hillsdon.svnwiki.wiki.renderer;

import junit.framework.TestCase;

public class TestCustomWikiLinkNode extends TestCase {

  public void test() {
    CustomWikiLinkNode node = new CustomWikiLinkNode(null, null);
    // We used to reject the 'See' and never consider the following text.
    assertNotNull(node.find("\nSee ConfigInterWikiLinks (ugly at present) for configuration."));
  }
  
}
