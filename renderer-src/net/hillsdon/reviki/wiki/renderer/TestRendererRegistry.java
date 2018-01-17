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

import junit.framework.TestCase;
import net.hillsdon.reviki.wiki.MarkupRenderer;

import java.io.InputStream;

public class TestRendererRegistry extends TestCase {

  private DelegatingRenderer _default;

  private RendererRegistry _registry;

  public void setUp() {
    _default = new DelegatingRenderer(null, null, null, null, null);
    _registry = new RendererRegistry(_default);
  }

  /**
   * Test that we can retrieve the default renderer.
   */
  public void testGetDefault() {
    assertNotNull(_registry.getDefaultRenderer());
    assertEquals(_default, _registry.getDefaultRenderer());
  }

  /**
   * Test that we can insert and retrieve renderers.
   */
  public void testRetrieve() {
    MarkupRenderer<InputStream> renderer = new RawRenderer();
    String ctype = "raw";

    assertFalse(_registry.hasRenderer(ctype));
    _registry.addRenderer(ctype, renderer);
    assertTrue(_registry.hasRenderer(ctype));
    assertEquals(renderer, _registry.getRenderer(ctype));
  }

  /**
   * Test that null ctypes are correctly handled.
   */
  public void testNull() {
    assertFalse(_registry.hasRenderer(null));
    assertNull(_registry.getRenderer(null));
  }
}
