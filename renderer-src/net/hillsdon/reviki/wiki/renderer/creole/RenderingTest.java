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
package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.SimplePageStore;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.WikiUrls;
import net.hillsdon.reviki.web.urls.impl.ExampleDotComWikiUrls;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class RenderingTest extends TestCase {
  protected PageStore pageStore;

  protected LinkPartsHandler linkHandler, imageHandler;

  protected Supplier<List<Macro>> macros;

  public RenderingTest() {
    WikiUrls wikiUrls = new ExampleDotComWikiUrls();

    InternalLinker linker = new InternalLinker(wikiUrls);
    InterWikiLinker wikilinker = new InterWikiLinker();
    wikilinker.addWiki("foo", "http://www.example.com/foo/Wiki?%s");
    pageStore = new SimplePageStore();

    LinkResolutionContext resolver = new LinkResolutionContext(linker, wikilinker, pageStore);

    linkHandler = new DummyLinkHandler(DummyLinkHandler.ANCHOR, resolver);
    imageHandler = new DummyLinkHandler(DummyLinkHandler.IMAGE, resolver);
    macros = Suppliers.ofInstance((List<Macro>) new LinkedList<Macro>());
  }
}