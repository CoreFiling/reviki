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
      throw new RuntimeException("Escaping should precude this error.", ex);
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
