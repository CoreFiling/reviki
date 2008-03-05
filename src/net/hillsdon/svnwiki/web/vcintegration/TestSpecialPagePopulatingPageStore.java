package net.hillsdon.svnwiki.web.vcintegration;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.SimplePageStore;

public class TestSpecialPagePopulatingPageStore extends TestCase {

  private static final PageReference FRONT_PAGE_REF = new PageReference("FrontPage");
  private SimplePageStore _delegate;
  private SpecialPagePopulatingPageStore _special;

  @Override
  protected void setUp() throws Exception {
    _delegate = new SimplePageStore();
    _special = new SpecialPagePopulatingPageStore(_delegate);
  }
  
  public void testAddsSpecialPagesToList() throws Exception {
    assertTrue(_special.list().contains(new PageReference("ConfigSvnLocation")));
  }
  
  public void testPopulatesSomePages() throws Exception {
    PageInfo frontPage = _special.get(FRONT_PAGE_REF, -1);
    assertTrue(frontPage.isNew());
    assertEquals(PageInfo.UNCOMMITTED, frontPage.getLastChangedRevision());
    assertTrue(frontPage.getContent().contains("Welcome to"));
  }
  
  public void testOnlyPopulatedThePageIfTheUnderlyingStoreDoesntHaveIt() throws Exception {
    _delegate.set(FRONT_PAGE_REF, "", -1, "foo", "an edit");
    assertEquals("foo", _special.get(FRONT_PAGE_REF, -1).getContent());
  }
  
}
