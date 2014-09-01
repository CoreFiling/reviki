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
package net.hillsdon.reviki.web.urls;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Can create links to pages on the same wiki given a page name.
 */
public class InternalLinker {

  private final SimpleWikiUrls _wikiUrls;

  public InternalLinker(final SimpleWikiUrls wikiUrls) {
    _wikiUrls = wikiUrls;
  }
  
  public URI uri(final String pageName) throws UnknownWikiException, URISyntaxException {
    return _wikiUrls.page(pageName);
  }
  
  public URI uri(final String pageName, final String query) throws UnknownWikiException, URISyntaxException {
    URI uri = _wikiUrls.page(pageName);
    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), query, uri.getFragment());
  }

}
