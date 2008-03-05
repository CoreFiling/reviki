package net.hillsdon.svnwiki.wiki.macros;

import static java.util.Collections.sort;
import static net.hillsdon.fij.core.Functional.list;
import static net.hillsdon.fij.core.Functional.map;
import static net.hillsdon.fij.text.Strings.join;

import java.util.List;

import net.hillsdon.svnwiki.search.SearchEngine;
import net.hillsdon.svnwiki.search.SearchMatch;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;

public class SearchMacro implements Macro {

  private final SearchEngine _searchEngine;

  public SearchMacro(final SearchEngine searchEngine) {
    _searchEngine = searchEngine;
  }
  
  public String getName() {
    return "search";
  }

  public String handle(final PageReference page, final String remainder) throws Exception {
    List<String> pages = list(map(_searchEngine.search(_searchEngine.escape(remainder), false), SearchMatch.TO_PAGE_NAME));
    sort(pages);
    return join(pages.iterator(), "  * ", "", "");
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

}
