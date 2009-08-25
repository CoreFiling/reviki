
package net.hillsdon.reviki.web.pages.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.eq;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.pages.DiffGenerator;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ResponseSessionURLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

import org.easymock.EasyMock;

/**
 * @author pjt
 *
 */
public class TestDefaultPageImplEditor extends TestCase {

  private static final String LOCK_TOKEN = "dooby";
  private static final PageReference THE_PAGE = new PageReferenceImpl("ThePage");
  private MockHttpServletRequest _request;
  private HttpServletResponse _response;
  private DefaultPageImpl _page;
  private CachingPageStore _store;
  private PageStore _pageStore;
  private MarkupRenderer _renderer;
  private WikiGraph _graph;
  private DiffGenerator _diffGenerator;
  private WikiUrls _wikiUrls;
  private ResultNode _resultNode;

  private FeedWriter _feedWriter;
  private PageInfoImpl _pageInfo;
  
  private static final String USERNAME = "user";

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _response = null;
    _store = createMock(CachingPageStore.class);
    _pageStore = createMock(PageStore.class);
    _pageInfo = new PageInfoImpl("ThePage", "content", 0, 0, "user", new Date(), "user", LOCK_TOKEN, null);
    _renderer = createMock(MarkupRenderer.class);
    _graph = createMock(WikiGraph.class);
    _diffGenerator = createMock(DiffGenerator.class);
    _wikiUrls = createMock(WikiUrls.class);
    _feedWriter = createMock(FeedWriter.class);
    _page = new DefaultPageImpl(null, _store, _renderer, _graph, _diffGenerator, _wikiUrls, _feedWriter);
    _resultNode = createMock(ResultNode.class);
    expect(_store.getUnderlying()).andStubReturn(_pageStore);
  }

  private void replay() {
    EasyMock.replay(_store);
    EasyMock.replay(_pageStore);
    EasyMock.replay(_renderer);
    EasyMock.replay(_graph);
    EasyMock.replay(_diffGenerator);
    EasyMock.replay(_wikiUrls);
    EasyMock.replay(_feedWriter);
    EasyMock.replay(_resultNode);
  }

  public void testInvalidSessionIdPreview() throws Exception {
    _request.setParameter(DefaultPageImpl.PARAM_LOCK_TOKEN, LOCK_TOKEN);
    _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "malicious content!");
    _request.setAttribute(RequestAttributes.USERNAME, USERNAME);
    _request.setParameter(DefaultPageImpl.SUBMIT_PREVIEW, "");
    _request.setParameter(DefaultPageImpl.PARAM_SESSION_ID, MockHttpServletRequest.MOCK_SESSION_ID + "AAA");

    expectTryToLock();
    expect(_diffGenerator.getDiffMarkup("content", "malicious content!" + Strings.CRLF)).andReturn("difference");
    replay();

    final JspView view = (JspView) _page.editor(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    final String preview = (String) _request.getAttribute(DefaultPageImpl.ATTR_PREVIEW);
    assertTrue("EditPage".equals(view.getName()));
    assertTrue(preview == null || preview.length() == 0);
    verify();
  }

  private void expectTryToLock() throws PageStoreException {
    // Not ideal that we lock twice but simple and only in an error case.
    expect(_pageStore.tryToLock(THE_PAGE)).andReturn(_pageInfo).atLeastOnce();
  }

  public void testPreviewShowsContent() throws Exception {
    _request.setParameter(DefaultPageImpl.PARAM_LOCK_TOKEN, LOCK_TOKEN);
    _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "new content");
    _request.setAttribute(RequestAttributes.USERNAME, USERNAME);
    _request.setParameter(DefaultPageImpl.SUBMIT_PREVIEW, "");
    _request.setParameter(DefaultPageImpl.PARAM_SESSION_ID, MockHttpServletRequest.MOCK_SESSION_ID);
    expect(_renderer.render(eq(THE_PAGE), eq("new content" + Strings.CRLF), isA(ResponseSessionURLOutputFilter.class))).andReturn(_resultNode);
    expect(_resultNode.toXHTML()).andReturn("rendered preview");
    expectTryToLock();
    replay();
    _page.editor(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    final String preview = (String) _request.getAttribute(DefaultPageImpl.ATTR_PREVIEW);
    assertEquals("rendered preview", preview);
  }
}
