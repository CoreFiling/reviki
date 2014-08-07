/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.wiki.renderer;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Suppliers;

public class TestRendererRegistry extends TestCase {

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
