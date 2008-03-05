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
package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class WikiGraphImpl implements WikiGraph {

  private final SearchEngine _searchEngine;
  private final PageStore _pageStore;

  public WikiGraphImpl(final PageStore pageStore, final SearchEngine searchEngine) {
    _pageStore = pageStore;
    _searchEngine = searchEngine;
  }
  
  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    return _searchEngine.incomingLinks(page);
  }

  public Set<String> outgoingLinks(String page) throws IOException, PageStoreException {
    return _searchEngine.outgoingLinks(page);
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

}
