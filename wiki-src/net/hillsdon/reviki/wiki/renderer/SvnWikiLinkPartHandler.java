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

import java.net.URISyntaxException;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.text.WikiWordUtils;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.Configuration;
import net.hillsdon.reviki.web.urls.InterWikiLinker;
import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;

public class SvnWikiLinkPartHandler implements LinkPartsHandler {

  public static final String IMAGE = "<img %sclass=\"%s\" src=\"%s\" alt=\"%s\" />";

  public static final String ANCHOR = "<a %sclass=\"%s\" href=\"%s\">%s</a>";

  private final InternalLinker _internalLinker;

  private final String _formatString;

  private final SimplePageStore _store;

  private final LinkResolutionContext _linkResolutionContext;

  private InterWikiLinker _interWikiLinker;

  private final Configuration _configuration;

  public SvnWikiLinkPartHandler(final String formatString, final LinkResolutionContext parentContext) {
    _formatString = formatString;
    _internalLinker = null;
    _store = parentContext.getPageStore();
    _configuration = null;
    _linkResolutionContext = parentContext;
  }

  public SvnWikiLinkPartHandler(final String formatString, final PageStore store, final InternalLinker internalLinker, final Configuration configuration) {
    _formatString = formatString;
    _internalLinker = internalLinker;
    _store = store;
    _configuration = configuration;
    _linkResolutionContext = null;
  }

  public LinkResolutionContext getContext() {
    if (_linkResolutionContext != null) {
      return _linkResolutionContext;
    }
    else {
      return new LinkResolutionContext(_internalLinker, _interWikiLinker, _configuration, _store);
    }
  }

  public String handle(final PageReference page, final String xhtmlContent, final LinkParts link, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    if (isAcronymNotLink(link)) {
      return link.getText();
    }

    LinkResolutionContext resolver = resolver(page);
    String noFollow = link.isNoFollow(resolver) ? "rel=\"nofollow\" " : "";
    String clazz = link.getStyleClass(resolver);

    String url = handle(page, link, urlOutputFilter);
    return String.format(_formatString, noFollow, Escape.html(clazz), url, xhtmlContent);
  }

  public String handle(PageReference page, LinkParts link, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    LinkResolutionContext resolver = resolver(page);
    String url = link.getURL(resolver);
    return urlOutputFilter.filterURL(url);
  }

  public boolean isAcronymNotLink(LinkParts link) {
    try {
      return (!link.getTarget().exists(_store) && WikiWordUtils.isAcronym(link.getText()));
    }
    catch (Exception e) {
      return true;
    }
  }

  /**
   * Lazily initialise interWikiLinker from configuration. Trying to do this in
   * the constructor causes an exception.
   */
  private InterWikiLinker interWikiLinker() {
    if (_configuration != null) {
      try {
        _interWikiLinker = _configuration.getInterWikiLinker();
      }
      catch (PageStoreException e) {
        throw new IllegalArgumentException(e);
      }
    }

    return _interWikiLinker;
  }

  /**
   * Produce a page-specific resolver.
   */
  private LinkResolutionContext resolver(final PageReference page) {
    if (_linkResolutionContext != null) {
      return _linkResolutionContext.derive(page);
    }
    else {
      return new LinkResolutionContext(_internalLinker, interWikiLinker(), _configuration, _store, page);
    }
  }

}
