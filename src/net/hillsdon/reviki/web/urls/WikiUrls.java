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

import net.hillsdon.reviki.configuration.WikiConfiguration;

/**
 * Wikis are rather tied up with the web.  At the moment we assume the public URL
 * is the servlet container URL which is probably dubious if apache is fronting
 * tomcat etc.  Probably need a configurable base URL.
 *
 * These methods return fully qualified URLs.
 *
 * @author mth
 */
public interface WikiUrls extends ResourceUrls {

  /**
   * Key for instance of this in the request.
   */
  String KEY = WikiUrls.class.getName();

  /**
   * Note that if the the returned String is going to be used as a link, it must be encoded
   * with a {@link URLOutputFilter}.
   * @return
   */
  String pagesRoot();

  String pagesRoot(String wikiName);

  String search(URLOutputFilter urlOutputFilter);

  String page(String wikiName, String pageName, String extraPath, String query, String fragment, URLOutputFilter urlOutputFilter);

  String page(String wikiName, String pageName, URLOutputFilter urlOutputFilter);

  String feed(URLOutputFilter urlOutputFilter);

  String interWikiTemplate();

  WikiConfiguration getWiki();

  String getWikiName();

}
