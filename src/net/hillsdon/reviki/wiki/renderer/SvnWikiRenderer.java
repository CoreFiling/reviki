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
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;

public class SvnWikiRenderer extends MarkupRenderer<String> {
  private final Configuration configuration;
  private final InternalLinker internalLinker;
  private final SvnWikiLinkPartHandler linkHandler;
  private final SvnWikiLinkPartHandler imageHandler;
  private final Supplier<List<Macro>> macros;
  private final PageStore pageStore;
  private final HtmlRenderer _renderer;

  public SvnWikiRenderer(final Configuration configuration, final PageStore pageStore, final InternalLinker internalLinker, final Supplier<List<Macro>> macros) {
    this.configuration = configuration;
    this.internalLinker = internalLinker;
    this.linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, pageStore, internalLinker, configuration);
    this.imageHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, pageStore, internalLinker, configuration);
    this.macros = macros;
    this.pageStore = pageStore;

    _renderer = new HtmlRenderer(pageStore, linkHandler, imageHandler, macros);
  }

  /**
   * Return the inner renderer.
   */
  public HtmlRenderer getRenderer() {
    return _renderer;
  }

  @Override
  public ASTNode render(final PageInfo page) throws IOException, PageStoreException {
    return _renderer.render(page);
  }

  @Override
  public String build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    return _renderer.build(ast, urlOutputFilter);
  }
}
