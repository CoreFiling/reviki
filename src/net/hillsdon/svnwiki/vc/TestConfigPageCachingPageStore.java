package net.hillsdon.svnwiki.vc;

import static net.hillsdon.svnwiki.vc.ConfigPageCachingPageStore.isConfigPage;
import junit.framework.TestCase;

public class TestConfigPageCachingPageStore extends TestCase {

  public void testIsConfigPage() {
    assertFalse(isConfigPage("Config"));
    assertTrue(isConfigPage("ConfigFoo"));
    assertFalse(isConfigPage("ConfiguringStuff"));
  }
  
}
