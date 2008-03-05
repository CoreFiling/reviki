package net.hillsdon.svnwiki.wiki.macros;

import static java.util.Collections.sort;
import static net.hillsdon.fij.text.Strings.join;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.macro.Macro;
import net.hillsdon.svnwiki.wiki.renderer.macro.ResultFormat;

public abstract class AbstractListOfPagesMacro implements Macro {

  public final String handle(final PageReference page, final String remainder) throws Exception {
    List<String> pages = new ArrayList<String>(getPages(remainder));
    sort(pages);
    return join(pages.iterator(), "  * ", "\n", "");
  }

  public final ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  protected abstract Collection<String> getPages(String remainder) throws Exception;

}
