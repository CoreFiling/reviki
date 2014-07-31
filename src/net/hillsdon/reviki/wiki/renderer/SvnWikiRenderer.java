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
import java.util.List;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Supplier;

public class SvnWikiRenderer extends MarkupRenderer<String> {
  private final RendererRegistry _registry;

  public SvnWikiRenderer(final Configuration configuration, final PageStore pageStore, final InternalLinker internalLinker, final Supplier<List<Macro>> macros) {
    final LinkPartsHandler linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, pageStore, internalLinker, configuration);
    final LinkPartsHandler imageHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, pageStore, internalLinker, configuration);

    HtmlRenderer html = new HtmlRenderer(pageStore, linkHandler, imageHandler, macros);
    DocbookRenderer docbook = new DocbookRenderer(pageStore, linkHandler, imageHandler, macros);
    RawRenderer raw = new RawRenderer();

    _registry = new RendererRegistry(html);
    _registry.addStreamOutputRenderer(ViewTypeConstants.CTYPE_DOCBOOK, new WrappedStreamRenderer(new WrappedXMLRenderer(docbook)));
    _registry.addStreamOutputRenderer(ViewTypeConstants.CTYPE_RAW, new WrappedStreamRenderer(raw));
  }

  /**
   * Return a source of renderers.
   */
  public RendererRegistry getRenderers() {
    return _registry;
  }

  @Override
  public ASTNode render(PageInfo page) throws IOException, PageStoreException {
    MarkupRenderer<String> renderer = _registry.getPageOutputRenderer(ViewTypeConstants.CTYPE_DEFAULT);
    return renderer.render(page);
  }

  @Override
  public String build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    MarkupRenderer<String> renderer = _registry.getPageOutputRenderer(ViewTypeConstants.CTYPE_DEFAULT);
    return renderer.build(ast, urlOutputFilter);
  }
}
