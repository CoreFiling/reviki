package net.hillsdon.svnwiki.wiki.macros;

import static java.util.Collections.sort;
import static net.hillsdon.fij.text.Strings.join;

import java.util.ArrayList;
import java.util.List;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.WikiGraph;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;

public class BackLinkListMacro implements Macro {

  private final WikiGraph _wikiGraph;

  public BackLinkListMacro(final WikiGraph wikiGraph) {
    _wikiGraph = wikiGraph;
  }
  
  public String getName() {
    return "backlinks";
  }

  public String handle(final PageReference page, final String remainder) throws Exception {
    List<String> backlinks = new ArrayList<String>(_wikiGraph.getBacklinks(remainder));
    sort(backlinks);
    return join(backlinks.iterator(), "  * ", "\n", "");
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

}
