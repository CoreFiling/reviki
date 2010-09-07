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
package net.hillsdon.reviki.wiki.graph;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.CachingPageStore;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class WikiGraphImpl implements WikiGraph {

  private final SearchEngine _searchEngine;
  private final PageStore _pageStore;

  public WikiGraphImpl(final CachingPageStore pageStore, final SearchEngine searchEngine) {
    _pageStore = pageStore;
    _searchEngine = searchEngine;
  }

  public Set<SearchMatch> incomingLinks(final String page) throws IOException, PageStoreException {
    Set<SearchMatch> incoming = _searchEngine.incomingLinks(page);
    return onlyExistingPages(incoming);
  }

  public Set<SearchMatch> outgoingLinks(final String page) throws IOException, PageStoreException {
    Set<SearchMatch> outgoing = _searchEngine.outgoingLinks(page);
    return onlyExistingPages(outgoing);
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
  private Set<SearchMatch> onlyExistingPages(final Set<SearchMatch> pages) throws PageStoreException {
    final Set<String> allExisting = ImmutableSet.copyOf(Iterables.transform(_pageStore.list(), PageReference.TO_NAME));
    return ImmutableSet.copyOf(Iterables.filter(pages, new Predicate<SearchMatch>() {

      public boolean apply(final SearchMatch searchMatch) {
        return allExisting.contains(searchMatch.getPage());
      }
    }));
  }

}
