package net.hillsdon.svnwiki.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;

public class TestExternalCommitAwareSearchEngine extends TestCase {

  private SearchEngine _mockedDelegate;
  private PageStore _mockedPageStore;
  private ExternalCommitAwareSearchEngine _externalCommitAware;

  @Override
  protected void setUp() throws Exception {
    _mockedDelegate = createMock(SearchEngine.class);
    _externalCommitAware = new ExternalCommitAwareSearchEngine(_mockedDelegate);
    _mockedPageStore = createMock(PageStore.class);
    _externalCommitAware.setPageStore(_mockedPageStore);
  }

  public void testSearchSkipsSyncIfNoPageStoreProvided() throws Exception {
    _externalCommitAware.setPageStore(null);
    expectAndVerifyJustDelegatedSearch();
  }
  
  public void testSearchSkipsSyncIfAlreadyIndexedLatestRevision() throws Exception {
    expect(_mockedDelegate.getHighestIndexedRevision()).andReturn(12L);
    expect(_mockedPageStore.getLatestRevision()).andReturn(12L);
    expectAndVerifyJustDelegatedSearch();
  }

  public void testAsksForAndIndexesMissedPagesWhenIndexedRevisionLessThanLatestRevision() throws Exception {
    expect(_mockedDelegate.getHighestIndexedRevision()).andReturn(10L);
    expect(_mockedPageStore.getLatestRevision()).andReturn(12L);
    PageReference edited = new PageReference("EditedPage");
    PageReference deleted = new PageReference("DeletedPage");
    expect(_mockedPageStore.getChangedBetween(11, 12)).andReturn(Arrays.asList(deleted, edited));
    expect(_mockedPageStore.get(deleted, 12)).andReturn(new PageInfo("DeletedPage", "", PageInfo.UNCOMMITTED, PageInfo.UNCOMMITTED, "mth", null, null, null));
    expect(_mockedPageStore.get(edited, 12)).andReturn(new PageInfo("EditedPage", "Edited content", 12, 11, "mth", new Date(0), null, null));

    _mockedDelegate.delete("DeletedPage", 12);
    _mockedDelegate.index("EditedPage", 12, "Edited content");

    expectAndVerifyJustDelegatedSearch();
  }
  
  private void expectAndVerifyJustDelegatedSearch() throws Exception {
    expect(_mockedDelegate.search("Whatever")).andReturn(Collections.<SearchMatch>emptySet());
    replay(_mockedDelegate, _mockedPageStore);
    _externalCommitAware.search("Whatever");
    verify(_mockedDelegate, _mockedPageStore);
  }
  
}
