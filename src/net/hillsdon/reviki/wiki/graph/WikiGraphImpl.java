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
package net.hillsdon.reviki.wiki.graph;

import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;

public class WikiGraphImpl implements WikiGraph {

  private final SearchEngine _searchEngine;
  private final PageStore _pageStore;

  public WikiGraphImpl(final PageStore pageStore, final SearchEngine searchEngine) {
    _pageStore = pageStore;
    _searchEngine = searchEngine;
  }
  
  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    Set<String> incoming = _searchEngine.incomingLinks(page);
    retainOnlyExistingPages(incoming);
    return incoming;
  }

  public Set<String> outgoingLinks(final String page) throws IOException, PageStoreException {
    Set<String> outgoing = _searchEngine.outgoingLinks(page);
    retainOnlyExistingPages(outgoing);
    return outgoing;
  }

  public Set<String> isolatedPages() throws IOException, PageStoreException {
    final Set<String> isolated = new LinkedHashSet<String>();
    for (PageReference page : _pageStore.list()) {
      final String name = page.getPath();
      if (incomingLinks(name).isEmpty()) {
        isolated.add(name);
      }
    }
    return isolated;
  }

  /**
   * The search index will only update its record of outgoing links
   * when pages are edited so we compensate by filtering.
   * 
   * @param pages Incoming.
   * @throws PageStoreException If we can't get the page list.
   */
  private void retainOnlyExistingPages(final Set<String> pages) throws PageStoreException {
    pages.retainAll(list(map(_pageStore.list(), PageReference.TO_NAME)));
  }

}
