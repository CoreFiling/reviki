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

import java.net.URISyntaxException;

import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public interface LinkPartsHandler {

  /** Render a LinkParts to HTML. */
  String handle(PageReference page, String xhtmlContent, LinkParts parts, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException;

  /** Render a LinkParts to a processed URI. */
  String handle(PageReference page, LinkParts parts, URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException;

  /** Get the context for resolving [inter]wiki links. */
  LinkResolutionContext getContext();

  /** Check if an acronym is actually a link. */
  boolean isAcronymNotLink(LinkParts parts);
}
