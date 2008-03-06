/**
 * Copyright 2007 Matthew Hillsdon
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
package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.UnknownWikiException;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkParts;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.svnwiki.wiki.renderer.creole.RenderNode;
import net.hillsdon.svnwiki.wiki.renderer.result.CompositeResultNode;

public class SvnWikiLinkPartHandler implements LinkPartsHandler {
  
  public static final String IMAGE = "<img class='%s' src='%s' alt='%s' />";
  public static final String ANCHOR = "<a class='%s' href='%s'>%s</a>";
  
  private final InternalLinker _internalLinker;
  private final Configuration _configuration;
  private final String _formatString;

  public SvnWikiLinkPartHandler(final String formatString, final InternalLinker internalLinker, final Configuration configuration) {
    _formatString = formatString;
    _internalLinker = internalLinker;
    _configuration = configuration;
  }
  
  public String handle(final PageReference page, final RenderNode renderer, final LinkParts link) {
    if (link.isURL()) {
      return link(page, renderer, "external", link.getRefd(), link.getText());
    }
    else {
      if (link.getWiki() != null) {
        try {
          return link(page, renderer, "inter-wiki", _configuration.getInterWikiLinker().url(link.getWiki(), link.getRefd()), link.getText());
        }
        catch (UnknownWikiException e) {
          return link.getText();
        }
        catch (PageStoreException e) {
          return link.getText();
        }
      }
      else {
        // Add page prefix if it is an attachment for the current page.
        String refd = link.getRefd();
        boolean hasPagePart = refd.contains("/");
        if (refd.contains(".") || hasPagePart) {
          if (!hasPagePart) {
            refd = page.getPath() + "/attachments/" + refd;
          }
          else {
            refd = refd.replaceFirst("/", "/attachments/");
          }
          return link(page, renderer, "attachment", refd, link.getText());
        }
        else {
          return _internalLinker.link(link.getRefd(), link.getText());
        }
      }
    }
  }
  
  private String link(final PageReference page, final RenderNode renderer, final String clazz, final String url, final String text) {
    return String.format(_formatString, Escape.html(clazz), Escape.html(url), new CompositeResultNode(renderer.render(page, text, null)).toXHTML());
  }

}
