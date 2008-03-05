package net.hillsdon.svnwiki.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;

public class TestSearchIndexPopulatingPageStore extends TestCase {

  private PageStore _mockedDelegate;
  private SearchEngine _mockedSearchEngine;
  private SearchIndexPopulatingPageStore _populatingPageStore;

  @Override
  protected void setUp() throws Exception {
    _mockedDelegate = createMock(PageStore.class);
    _mockedSearchEngine = createMock(SearchEngine.class);
    _populatingPageStore = new SearchIndexPopulatingPageStore(_mockedSearchEngine, _mockedDelegate);
  }
  
  public void testDelegatesThenIndexesAgainstNewRevisionNumber() throws Exception {
    final PageReference ref = new PageReference("SomePage");
    final String lockToken = "";
    final long baseRevision = 12;
    final String content = "New content";
    final String commitMessage = "Did something";
    
    final long newRevision = 15;
  
    expect(_mockedDelegate.set(ref, lockToken, baseRevision, content, commitMessage)).andReturn(newRevision );
    _mockedSearchEngine.index(ref.getPath(), newRevision, content);
    replay(_mockedDelegate, _mockedSearchEngine);
    _populatingPageStore.set(ref, lockToken, baseRevision, content, commitMessage);
    verify(_mockedDelegate, _mockedSearchEngine);
  }
  
}
