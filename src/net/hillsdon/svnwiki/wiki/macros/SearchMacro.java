package net.hillsdon.svnwiki.wiki.macros;

import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;

import java.util.Collection;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.search.SearchMatch;

public class SearchMacro extends AbstractListOfPagesMacro {

  private final SearchEngine _searchEngine;

  public SearchMacro(final SearchEngine searchEngine) {
    _searchEngine = searchEngine;
  }
  
  public String getName() {
    return "search";
  }

  @Override
  protected Collection<String> getPages(final String remainder) throws Exception {
    return list(map(_searchEngine.search(_searchEngine.escape(remainder), false), SearchMatch.TO_PAGE_NAME));
  }

}
