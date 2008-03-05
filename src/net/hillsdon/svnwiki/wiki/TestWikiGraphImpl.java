package net.hillsdon.svnwiki.wiki;

import static net.hillsdon.fij.core.Functional.set;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import junit.framework.TestCase;
import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.SimplePageStore;

import org.easymock.EasyMock;

public class TestWikiGraphImpl extends TestCase {

  private PageStore _store;
  private SearchEngine _mockedSearchEngine;
  
  private WikiGraph _graph;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore();
    _store.set(new PageReference("FooPage"), "", -1, "Foo content", "");
    _mockedSearchEngine = EasyMock.createMock(SearchEngine.class);
    _graph = new WikiGraphImpl(_store, _mockedSearchEngine);
  }
  
  public void testRemovesNonExistantPagesFromOutgoingLinks() throws Exception {
    expect(_mockedSearchEngine.outgoingLinks("RootPage")).andReturn(set("FooPage", "BarPage"));
    replay(_mockedSearchEngine);
    assertEquals(set("FooPage"), _graph.outgoingLinks("RootPage"));
  }
  
  public void testRemovesNonExistantPagesFromIncomingLinks() throws Exception {
    expect(_mockedSearchEngine.incomingLinks("RootPage")).andReturn(set("FooPage", "BarPage"));
    replay(_mockedSearchEngine);
    assertEquals(set("FooPage"), _graph.incomingLinks("RootPage"));
  }

}
