
package net.hillsdon.reviki.web.pages.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.eq;

import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.hillsdon.fij.text.Strings;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.VersionedPageInfoImpl;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.common.ConsumedPath;
import net.hillsdon.reviki.web.common.JspView;
import net.hillsdon.reviki.web.common.MockHttpServletRequest;
import net.hillsdon.reviki.web.common.RequestAttributes;
import net.hillsdon.reviki.web.pages.DiffGenerator;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.wiki.feeds.FeedWriter;
import net.hillsdon.reviki.wiki.graph.WikiGraph;
import net.hillsdon.reviki.wiki.renderer.DelegatingRenderer;
import net.hillsdon.reviki.wiki.renderer.RendererRegistry;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;

import org.easymock.EasyMock;

import com.google.common.collect.ImmutableList;

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
  private DelegatingRenderer _renderer;
  private RendererRegistry _renderers;
  private WikiGraph _graph;
  private DiffGenerator _diffGenerator;
  private WikiUrls _wikiUrls;

  private FeedWriter _feedWriter;
  private VersionedPageInfoImpl _pageInfo;

  private static final String USERNAME = "user";

  @Override
  protected void setUp() throws Exception {
    _request = new MockHttpServletRequest();
    _response = null;
    _store = createMock(CachingPageStore.class);
    _pageStore = createMock(PageStore.class);
    _pageInfo = new VersionedPageInfoImpl("wiki", "ThePage", "content", 0, 0, "user", new Date(), "user", LOCK_TOKEN, null);
    _renderer = new DelegatingRenderer(_pageStore, null, null, null, null);
    _renderers = new RendererRegistry(_renderer);
    _graph = createMock(WikiGraph.class);
    _diffGenerator = createMock(DiffGenerator.class);
    _wikiUrls = createMock(WikiUrls.class);
    _feedWriter = createMock(FeedWriter.class);
    _page = new DefaultPageImpl(null, _store, _renderers, _graph, _diffGenerator, _wikiUrls, _feedWriter, null);
    expect(_store.getUnderlying()).andStubReturn(_pageStore);
  }

  private ASTNode mkResult(String content) {
    return new Page(ImmutableList.of((ASTNode) new Paragraph(new Inline(ImmutableList.of((ASTNode) new Plaintext(content))))));
  }

  private void replay() {
    EasyMock.replay(_store);
    EasyMock.replay(_pageStore);
    EasyMock.replay(_graph);
    EasyMock.replay(_diffGenerator);
    EasyMock.replay(_wikiUrls);
    EasyMock.replay(_feedWriter);
  }

  public void testInvalidSessionIdPreview() throws Exception {
    _request.setParameter(DefaultPageImpl.PARAM_LOCK_TOKEN, LOCK_TOKEN);
    _request.setParameter(DefaultPageImpl.PARAM_CONTENT, "malicious content!");
    _request.setParameter(DefaultPageImpl.PARAM_ATTRIBUTES, "");
    _request.setParameter(DefaultPageImpl.PARAM_ORIGINAL_ATTRIBUTES, "");
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
    _request.setParameter(DefaultPageImpl.PARAM_ATTRIBUTES, "");
    _request.setParameter(DefaultPageImpl.PARAM_ORIGINAL_ATTRIBUTES, "");
    _request.setAttribute(RequestAttributes.USERNAME, USERNAME);
    _request.setParameter(DefaultPageImpl.SUBMIT_PREVIEW, "");
    _request.setParameter(DefaultPageImpl.PARAM_SESSION_ID, MockHttpServletRequest.MOCK_SESSION_ID);
    assertTrue(_renderer.parse(new PageInfoImpl("", THE_PAGE.getPath(), "rendered preview" + Strings.CRLF, Collections.<String, String>emptyMap())).equals(mkResult("rendered preview")));
    expect(_diffGenerator.getDiffMarkup(eq("content"), eq("new content" + Strings.CRLF))).andReturn("rendered diff");
    expectTryToLock();
    replay();
    _page.editor(THE_PAGE, ConsumedPath.EMPTY, _request, _response);
    final String diff = (String) _request.getAttribute(DefaultPageImpl.ATTR_MARKED_UP_DIFF);
    assertEquals("rendered diff", diff);
  }
}
