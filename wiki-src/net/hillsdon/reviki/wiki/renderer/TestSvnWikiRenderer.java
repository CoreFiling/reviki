package net.hillsdon.reviki.wiki.renderer;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;

import com.google.common.base.Suppliers;

import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import junit.framework.TestCase;

public class TestSvnWikiRenderer extends TestCase {

  private SvnWikiRenderer _renderer;

  private RendererRegistry _renderers;

  public void setUp() {
    _renderer = new SvnWikiRenderer(new FakeConfiguration(), new SimplePageStore(), new InternalLinker(new ExampleDotComWikiUrls()), Suppliers.ofInstance(Collections.<Macro> emptyList()));
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

  /**
   * Test that we can retrieve a renderer for every (visible) ctype (other than
   * DEFAULT), and they're all different.
   */
  public void testCtypeRenderers() {
    HashSet<MarkupRenderer<InputStream>> seen = new HashSet<MarkupRenderer<InputStream>>();

    for (String ctype : ViewTypeConstants.CTYPES) {
      if (ctype.equals(ViewTypeConstants.CTYPE_DEFAULT)) {
        continue;
      }

      assertTrue("Missing renderer for " + ctype, _renderers.hasRenderer(ctype));

      MarkupRenderer<InputStream> renderer = _renderers.getRenderer(ctype);
      assertNotNull("Null renderer for " + ctype, renderer);

      assertFalse("Duplicate renderer for " + ctype, seen.contains(renderer));

      seen.add(renderer);
    }
  }

}
