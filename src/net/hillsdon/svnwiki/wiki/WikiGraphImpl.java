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

import net.hillsdon.svnwiki.search.QuerySyntaxException;
import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.search.SearchMatch;
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
  
  public Set<String> getBacklinks(final String page) throws IOException, PageStoreException {
    try {
      Set<SearchMatch>  backlinks = _searchEngine.search(_searchEngine.escape(page), false);
      backlinks.remove(new SearchMatch(page, null));
      final Set<String> pages = new LinkedHashSet<String>(backlinks.size());
      for (SearchMatch match : backlinks) {
        pages.add(match.getPage());
      }
      return pages;
    }
    catch (QuerySyntaxException ex) {
      throw new RuntimeException("Escaping should preclude this error.", ex);
    }
  }

  public Set<String> getIsolatedPages() throws IOException, PageStoreException {
    final Set<String> isolated = new LinkedHashSet<String>();
    for (PageReference page : _pageStore.list()) {
      final String name = page.getPath();
      if (getBacklinks(name).isEmpty()) {
        isolated.add(name);
      }
    }
    return isolated;
  }

}
