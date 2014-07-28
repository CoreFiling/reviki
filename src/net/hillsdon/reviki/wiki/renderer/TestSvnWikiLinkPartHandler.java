package net.hillsdon.reviki.wiki.renderer;

import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import junit.framework.TestCase;

public class TestSvnWikiLinkPartHandler extends TestCase {
  private SimplePageStore _store;
  private InternalLinker _internalLinker;
  private InterWikiLinker _interWikiLinker;
  private SvnWikiLinkPartHandler _handler;

  public void setUp() throws Exception {
    _store = new SimplePageStore("wiki");
    _store.get(new PageReferenceImpl("EX"), 1);
    _internalLinker = new InternalLinker(new ExampleDotComWikiUrls());
    _interWikiLinker = new InterWikiLinker();
    _interWikiLinker.addWiki("other", "other.example.com/%s");
    _handler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, _store, _internalLinker, _interWikiLinker);
  }

  public void testForeignWikiPageAlwaysExists() throws Exception {
    String html = _handler.handle(new PageInfoImpl("SomePage"), "Page", new LinkParts("Page", "other", "Page", null, null), URLOutputFilter.NULL);
    assertTrue(html.contains("inter-wiki"));
  }

  public void testWikiPageNotExists() throws Exception {
    String html = _handler.handle(new PageInfoImpl("SomePage"), "Page", new LinkParts("Page", null, "Page", null, null), URLOutputFilter.NULL);
    assertFalse(html.contains("existing-page"));
    assertFalse(html.contains("inter-wiki"));
  }

  public void testAcronymThatExists() throws Exception {
    /* Should be a link and should be marked as existing */
    String html = _handler.handle(new PageInfoImpl("SomePage"), "EX", new LinkParts("EX", null, "EX", null, null), URLOutputFilter.NULL);
    assertTrue(html.contains("<a"));
    assertTrue(html.contains("existing-page"));
  }

  public void testAcronymNotExists() throws Exception {
    /* Shouldn't even be a link */
    String html = _handler.handle(new PageInfoImpl("SomePage"), "ABC", new LinkParts("ABC", null, "ABC", null, null), URLOutputFilter.NULL);
    assertFalse(html.contains("<a"));
    assertFalse(html.contains("existing-page"));
    assertFalse(html.contains("inter-wiki"));
  }

  public void testAcronymForeign() throws Exception {
    /* Should be a link and should be marked as existing */
    String html = _handler.handle(new PageInfoImpl("SomePage"), "ABC", new LinkParts("ABC", "other", "ABC", null, null), URLOutputFilter.NULL);
    assertTrue(html.contains("<a"));
    assertTrue(html.contains("inter-wiki"));
  }
}
