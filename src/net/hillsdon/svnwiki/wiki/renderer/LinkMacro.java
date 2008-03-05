package net.hillsdon.svnwiki.wiki.renderer;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;

public class LinkMacro implements Macro {

  public String getName() {
    return "link";
  }

  public String handle(final PageReference page, final String remainder) {
    int pipeIndex = remainder.indexOf('|');
    String name = remainder.substring(0, pipeIndex);
    String url = remainder.substring(pipeIndex + 1, remainder.length());
    return String.format("<a href='%s'>%s</a>", Escape.html(url), Escape.html(name));
  }

}
