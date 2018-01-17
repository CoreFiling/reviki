package net.hillsdon.reviki.wiki.renderer;

import java.util.Collections;
import com.google.common.base.Suppliers;

import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import junit.framework.TestCase;

public class TestSvnWikiRenderer extends TestCase {

  private SvnWikiRenderer _renderer;

  private RendererRegistry _renderers;

  public void setUp() {
    _renderer = new SvnWikiRenderer(new FakeConfiguration(), new SimplePageStore(), new InternalLinker(new ExampleDotComWikiUrls()), Suppliers.ofInstance(Collections.<Macro> emptyList()), null);
    _renderers = _renderer.getRenderers();
  }

  /**
   * Test that the renderer registry is not null.
   */
  public void testGetRenderer() {
    assertNotNull(_renderers);
  }

  /**
   * Test that we can retrieve the default renderer.
   */
  public void testGetDefault() {
    assertNotNull(_renderers.getDefaultRenderer());
  }
}
