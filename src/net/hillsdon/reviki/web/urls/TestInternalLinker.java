package net.hillsdon.reviki.web.urls;

import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import junit.framework.TestCase;

/**
 * Test for {@link InterWikiLinker}.
 *
 * @author js
 */
public class TestInternalLinker extends TestCase {

  private InternalLinker _linker;
  private SimplePageStore _store;

  @Override
  protected void setUp() throws Exception {
    _store = new SimplePageStore("wiki");
    _store.get(new PageReferenceImpl("EX"), 1);
    _linker = new InternalLinker(new ExampleDotComWikiUrls(), _store);
  }

  public void testForeignWikiPageAlwaysExists() throws Exception {
    URLOutputFilter urlOutputFilter = new URLOutputFilter() {
      public String filterURL(String url) {
        return url;
      }
    };
    String html = _linker.aHref("other", "Page", "", null , null, null, urlOutputFilter);
    assertTrue(html.contains("existing-page"));
  }

  public void testWikiPageNotExists() throws Exception {
    URLOutputFilter urlOutputFilter = new URLOutputFilter() {
      public String filterURL(String url) {
        return url;
      }
    };
    String html = _linker.aHref(null, "Page", "", null, null, null, urlOutputFilter);
    assertFalse(html.contains("existing-page"));
  }

  public void testAcronymThatExists() throws Exception {
    /* Should be a link and should be marked as existing */
    URLOutputFilter urlOutputFilter = new URLOutputFilter() {
      public String filterURL(String url) {
        return url;
      }
    };
    String html = _linker.aHref(null, "EX", "", null, null, null, urlOutputFilter);
    assertTrue(html.contains("<a"));
    assertTrue(html.contains("existing-page"));
  }

  public void testAcronymNotExists() throws Exception {
    /* Shouldn't even be a link */
    URLOutputFilter urlOutputFilter = new URLOutputFilter() {
      public String filterURL(String url) {
        return url;
      }
    };
    String html = _linker.aHref(null, "ABC", "", null, null, null, urlOutputFilter);
    assertFalse(html.contains("<a"));
    assertFalse(html.contains("existing-page"));
  }

  public void testAcronymForeign() throws Exception {
    /* Should be a link and should be marked as existing */
    URLOutputFilter urlOutputFilter = new URLOutputFilter() {
      public String filterURL(String url) {
        return url;
      }
    };
    String html = _linker.aHref("other", "ABC", "", null, null, null, urlOutputFilter);
    assertTrue(html.contains("<a"));
    assertTrue(html.contains("existing-page"));
  }

}
