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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.vc.impl.DummyPageStore;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.SimpleWikiUrls;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public abstract class RenderingTest extends TestCase {
  protected SimplePageStore pageStore;

  protected LinkPartsHandler linkHandler, imageHandler;

  protected Supplier<List<Macro>> macros;

  public RenderingTest() {
    SimpleWikiUrls wikiUrls = new SimpleWikiUrls() {
      public String pagesRoot() {
        return "http://www.example.com/reviki/pages/test-wiki/";
      }

      public URI page(String pageName) {
        URI root = URI.create(pagesRoot());
        try {
          String path = root.getPath();
          if (!path.endsWith("/")) {
            path = path + "/";
          }
          return new URI(root.getScheme(), root.getUserInfo(), root.getHost(), root.getPort(), path + pageName, root.getQuery(), root.getFragment());
        }
        catch (URISyntaxException e) {
          throw new RuntimeException(e);
        }
      }
    };

    InternalLinker linker = new InternalLinker(wikiUrls);
    InterWikiLinker wikilinker = new InterWikiLinker();
    wikilinker.addWiki("foo", "http://www.example.com/foo/Wiki?%s");
    pageStore = new DummyPageStore();

    LinkResolutionContext resolver = new LinkResolutionContext(linker, wikilinker, pageStore);

    linkHandler = new SimpleAnchors(resolver);
    imageHandler = new SimpleImages(resolver);
    macros = Suppliers.ofInstance((List<Macro>) new LinkedList<Macro>());
  }
}
