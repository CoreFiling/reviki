package net.hillsdon.reviki.web.pages.impl;

import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.pages.DefaultPage;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;

import org.easymock.EasyMock;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;

public class TestRecentChanges extends TestCase {

  private DefaultPage _defaultPage;
  private CachingPageStore _store;
  private FeedWriter _feedWriter;
  private WikiUrls _wikiUrls;
  private RecentChanges _recentChanges;

  @Override
  protected void setUp() throws Exception {
    _defaultPage = EasyMock.createMock(DefaultPage.class);
    _store = EasyMock.createMock(CachingPageStore.class);
    _feedWriter = EasyMock.createMock(FeedWriter.class);
    _wikiUrls = EasyMock.createMock(WikiUrls.class);
    _recentChanges = new RecentChanges(null, _defaultPage, _store, _feedWriter, _wikiUrls);
  }
  
  private void replay() {
    EasyMock.replay(_defaultPage, _store, _feedWriter, _wikiUrls);
  }
  
  public void testNoLimitUsesDefault() throws Exception {
    checkLimitUsed(RecentChanges.RECENT_CHANGES_DEFAULT_HISTORY_SIZE, null);
  }
  
  public void testInRangeLimitSpecifiedUsesLimit1() throws Exception {
    checkLimitUsed(RecentChanges.RECENT_CHANGES_MAX_HISTORY_SIZE);
  }
  public void testInRangeLimitSpecifiedUsesLimit2() throws Exception {
    checkLimitUsed(RecentChanges.RECENT_CHANGES_MAX_HISTORY_SIZE - 1);
  }
  public void testOverLimitSpecifiedUsesMax() throws Exception {
    checkLimitUsed(RecentChanges.RECENT_CHANGES_MAX_HISTORY_SIZE, String.valueOf(RecentChanges.RECENT_CHANGES_MAX_HISTORY_SIZE + 1));
  }
 
  private void checkLimitUsed(final long value) throws PageStoreException, Exception {
    checkLimitUsed(value, String.valueOf(value));
  }
  private void checkLimitUsed(final long expectedUsedLimitValue, final String limitParameter) throws PageStoreException, Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("limit", limitParameter);
    expect(_store.recentChanges(expectedUsedLimitValue)).andReturn(Collections.<ChangeInfo>emptyList());
    replay();
    _recentChanges.get(null, null, request, null);
    verify(_store);
  }
  
}
