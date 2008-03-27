package net.hillsdon.reviki.web.pages.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.pages.PageSource;
import net.hillsdon.reviki.web.pages.SpecialPage;
import net.hillsdon.reviki.web.pages.SpecialPages;

public class TestPageSourceImpl extends TestCase {
  
  private SpecialPages _specialPages;
  private DefaultPage _defaultPage;

  private PageSource _pageSource;

  @Override
  protected void setUp() throws Exception {
    _specialPages = createMock(SpecialPages.class);
    _defaultPage = createMock(DefaultPage.class);
    _pageSource = new PageSourceImpl(_specialPages, _defaultPage);
  }
  
  public void testReturnsSpecialPageIfAvailable() {
    SpecialPage specialPage = createMock(SpecialPage.class);
    expect(_specialPages.get("TheSpecialPage")).andReturn(specialPage).atLeastOnce();
    replay(_specialPages, specialPage, _defaultPage);
    assertSame(specialPage, _pageSource.get(new PageReference("TheSpecialPage")));
  }
  
  public void testFallsbackToDefaultPage() {
    expect(_specialPages.get("AnOrdinaryPage")).andReturn(null).atLeastOnce();
    replay(_specialPages, _defaultPage);
    assertSame(_defaultPage, _pageSource.get(new PageReference("AnOrdinaryPage")));
  }
  
}
