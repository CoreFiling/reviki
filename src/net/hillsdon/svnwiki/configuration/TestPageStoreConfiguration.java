package net.hillsdon.svnwiki.configuration;

import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.vc.SimplePageStore;

public class TestPageStoreConfiguration extends TestCase {

  private SimplePageStore _store;
  private PageStoreConfiguration _configuration;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore();
    _configuration = new PageStoreConfiguration(_store);
  }

  private void assertNoInterWikiLinks() throws PageStoreException {
    assertTrue(_configuration.getInterWikiLinker().getWikiToFormatStringMap().isEmpty());
  }
  
  public void testInterWikiLinkerEmptyWhenNoPage() throws Exception {
    assertNoInterWikiLinks();
  }
  
  public void testAddingPagePopulatesInterWikiLinker() throws Exception  {
    _store.set("ConfigInterWikiLinks", "", -1, "c2 http://c2.com/cgi/wiki?%s\r\n", "");
    assertEquals(Collections.singletonMap("c2", "http://c2.com/cgi/wiki?%s"), _configuration.getInterWikiLinker().getWikiToFormatStringMap());
  }
  
  // Currently most things are considered valid, we split on first whitespace...
  public void testInvalidEntryIgnored() throws Exception {
    _store.set("ConfigInterWikiLinks", "", -1, "nospace\r\n", "");
    assertNoInterWikiLinks();
  }
  
}
