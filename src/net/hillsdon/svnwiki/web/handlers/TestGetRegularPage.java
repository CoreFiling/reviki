package net.hillsdon.svnwiki.web.handlers;

import static net.hillsdon.fij.core.Functional.set;
import static net.hillsdon.svnwiki.web.handlers.GetRegularPage.MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import java.io.Writer;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.vc.PageInfo;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.web.common.ConsumedPath;
import net.hillsdon.svnwiki.web.common.JspView;
import net.hillsdon.svnwiki.web.common.MockHttpServletRequest;
import net.hillsdon.svnwiki.web.common.RequestParameterReaders;
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

  private MockHttpServletRequest _request;
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

  /**
   * The usual case of viewing an existing page.
   */
  public void testNoRevisionNoDiffViewsHead() throws Exception {
    // We should get all the links.
    expectGetIncomingLinks(getCountIncomingLinks(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY));
    PageInfo expectedPageInfo = expectGetContent();
    expectRenderContent();
    replay();
    JspView view = (JspView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, THE_PAGE);
    assertEquals("ViewPage", view.getName());
    // Check data provided to view.
    assertNotNull(_request.getAttribute(GetRegularPage.ATTR_RENDERED_CONTENTS));
    assertEquals(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY, ((Collection<?>) _request.getAttribute(GetRegularPage.ATTR_BACKLINKS)).size());
    assertNull(_request.getAttribute(GetRegularPage.ATTR_BACKLINKS_LIMITED));
    assertSame(expectedPageInfo, _request.getAttribute(GetRegularPage.ATTR_PAGE_INFO));
    verify();
  }

  public void testLimitsBacklinksIfMoreThanBackLinkLimitAndAddsAttributeToRequestIndicatingTheLimitWasReached() throws Exception {
    expectGetIncomingLinks(getCountIncomingLinks(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY + 1));
    expectGetContent();
    expectRenderContent();
    replay();
    _page.handlePage(ConsumedPath.EMPTY, _request, _response, THE_PAGE);
    assertEquals(MAX_NUMBER_OF_BACKLINKS_TO_DISPLAY, ((Collection<?>) _request.getAttribute(GetRegularPage.ATTR_BACKLINKS)).size());
    assertNotNull(_request.getAttribute(GetRegularPage.ATTR_BACKLINKS_LIMITED));
    verify();
  }

  public void testProvideRevisionAndDiffViewsDiffBetweenTheRevisions() throws Exception {
    _request.setParameter(RequestParameterReaders.PARAM_REVISION, "6");
    _request.setParameter(GetRegularPage.PARAM_DIFF_REVISION, "4");
    expectGetIncomingLinks();
    expectGetContent(6, "Content at revision six.");
    expectGetContent(4, "Content at revision four.");
    // We don't render anything.
    replay();
    JspView view = (JspView) _page.handlePage(ConsumedPath.EMPTY, _request, _response, THE_PAGE);
    assertEquals("ViewDiff", view.getName());
    assertNotNull(_request.getAttribute(GetRegularPage.ATTR_MARKED_UP_DIFF));
    verify();
  }
  
  private String[] getCountIncomingLinks(final int count) {
    String[] incomingLinks = new String[count];
    for (int i = 0; i < incomingLinks.length; ++i) {
      incomingLinks[i] = "IncomingLink" + (i + 1);
    }
    return incomingLinks;
  }
  
  private void verify() {
    EasyMock.verify(_store, _renderer, _graph);
  }

  private void replay() {
    EasyMock.replay(_store, _renderer, _graph);
  }
  
  private void expectRenderContent() throws Exception  {
    _renderer.render(eq(THE_PAGE), eq("Content"), isA(Writer.class));
    expectLastCall().once();
  }

  private void expectGetIncomingLinks(final String... returnedPages) throws Exception  {
    expect(_graph.incomingLinks(THE_PAGE.getPath())).andReturn(set(returnedPages)).once();
  }

  private PageInfo expectGetContent() throws Exception  {
    return expectGetContent(-1, "Content");
  }
  
  private PageInfo expectGetContent(final int revision, final String content) throws Exception  {
    PageInfo pageInfo = new PageInfo(THE_PAGE.getPath(), content, revision, revision, "", new Date(), "", "");
    expect(_store.get(THE_PAGE, revision)).andReturn(pageInfo).once();
    return pageInfo;
  }
  
}
