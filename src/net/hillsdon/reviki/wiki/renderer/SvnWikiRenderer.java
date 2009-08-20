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

import net.hillsdon.fij.accessors.Accessor;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleImageNode;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class SvnWikiRenderer implements MarkupRenderer {

  private final Configuration _configuration;
  private final InternalLinker _internalLinker;
  private final CreoleRenderer _creole;
  
  public SvnWikiRenderer(final Configuration configuration, final InternalLinker internalLinker,  final Accessor<List<Macro>> macros) {
    _configuration = configuration;
    _internalLinker = internalLinker;
    final SvnWikiLinkPartHandler linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, _internalLinker, _configuration);
    _creole = new CreoleRenderer(
        new RenderNode[] {
            new UnescapedHtmlNode(true),
            new JavaSyntaxHighlightedNode(true),
            new MacroNode(macros, true),
        },
        new RenderNode[] {
            new JavaSyntaxHighlightedNode(false),
            new UnescapedHtmlNode(false),
            new CreoleImageNode(new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, _internalLinker, _configuration)),
            new CreoleLinkNode(linkHandler),
            new CustomWikiLinkNode(linkHandler),
            new MacroNode(macros, false),
        });
  }
  
  public ResultNode render(final PageReference page, final String in, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return _creole.render(page, in, urlOutputFilter);
  }

}
