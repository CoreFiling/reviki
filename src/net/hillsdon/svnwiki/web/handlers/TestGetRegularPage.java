package net.hillsdon.svnwiki.web.handlers;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import java.io.Writer;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.MockHttpServletRequest;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.graph.WikiGraph;

import org.easymock.EasyMock;

/**
 * Tests for {@link GetRegularPage}.
 * 
 * @author mth
 */
public class TestGetRegularPage extends TestCase {

  private static final PageReference THE_PAGE = new PageReference("ThePage");
  
  private PageStore _store;
  private MarkupRenderer _renderer;
  private WikiGraph _graph;

  private HttpServletRequest _request;
  private HttpServletResponse _response;
  
  private GetRegularPage _page;

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _response = null;
    _store = createMock(PageStore.class);
    _renderer = createMock(MarkupRenderer.class);
    _graph = createMock(WikiGraph.class);
    _page = new GetRegularPage(_store, _renderer, _graph);
  }
  
  public void testNoRevisionNoDiffViewsHead() throws Exception {
    expect(_graph.incomingLinks(THE_PAGE.getPath())).andReturn(singleton("IncomingLinkToThePage")).once();
    expect(_store.get(THE_PAGE, -1)).andReturn(new PageInfo(THE_PAGE.getPath(), "Content", -1, -1, "", new Date(), "", ""));
    _renderer.render(eq(THE_PAGE), eq("Content"), isA(Writer.class));
    expectLastCall().once();
    replay();
    _page.handlePage(ConsumedPath.EMPTY, _request, _response, THE_PAGE);
    // Check data provided to view.
    assertNotNull(_request.getAttribute(GetRegularPage.ATTR_RENDERED_CONTENTS));
    assertEquals(asList("IncomingLinkToThePage"), _request.getAttribute(GetRegularPage.ATTR_BACKLINKS));
    verify();
  }
  
  private void verify() {
    EasyMock.verify(_store, _renderer, _graph);
  }

  private void replay() {
    EasyMock.replay(_store, _renderer, _graph);
  }
  
}
