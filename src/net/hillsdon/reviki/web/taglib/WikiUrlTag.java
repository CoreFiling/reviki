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
package net.hillsdon.reviki.web.taglib;


import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.jsp.JspException;

import net.hillsdon.reviki.web.urls.InternalLinker;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.links.LinkResolutionContext;

/**
 * Uses an {@link InternalLinker} to create URLs to wiki pages.
 *
 * @copyright
 * @author mth
 */
public class WikiUrlTag extends AbstractWikiLinkTag {

  private static final long serialVersionUID = 1L;

  protected String doOutput(LinkResolutionContext resolver, URLOutputFilter urlOutputFilter) throws JspException {
    URI uri;
    try {
      uri = resolver.resolve(null, getPage());
      String path = uri.getPath() + (getExtraPath()==null ? "" : getExtraPath());
      return urlOutputFilter.filterURL(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, getQuery(), uri.getFragment()).toASCIIString());
    }
    catch (UnknownWikiException e) {
      throw new JspException(e);
    }
    catch (URISyntaxException e) {
      throw new JspException(e);
    }
  }

}

