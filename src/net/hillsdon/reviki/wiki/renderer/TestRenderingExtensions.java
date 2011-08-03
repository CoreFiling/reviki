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

import java.io.IOException;
import java.util.Collections;

import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.creole.JsonDrivenRenderingTest;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import org.codehaus.jackson.JsonParseException;

import com.google.common.base.Suppliers;

public class TestRenderingExtensions extends JsonDrivenRenderingTest {

  public TestRenderingExtensions() throws JsonParseException, IOException {
    super(TestRenderingExtensions.class.getResource("rendering-extensions.json"));
  }

  @Override
  protected String render(final String input) throws IOException, PageStoreException {
    SvnWikiRenderer renderer = new SvnWikiRenderer(new FakeConfiguration(), new SimplePageStore(), new InternalLinker(new ExampleDotComWikiUrls()), Suppliers.ofInstance(Collections.<Macro> emptyList()));
    return renderer.render(new PageInfoImpl("", "", input, Collections.<String, String>emptyMap()), URLOutputFilter.NULL).toXHTML();
  }

}
