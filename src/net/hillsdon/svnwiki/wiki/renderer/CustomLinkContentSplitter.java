package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.MatchResult;

import net.hillsdon.svnwiki.wiki.renderer.creole.LinkContentSplitter;
import net.hillsdon.svnwiki.wiki.renderer.creole.LinkParts;

public class CustomLinkContentSplitter implements LinkContentSplitter {

  public LinkParts split(final MatchResult in) {
    String wikiName = in.group(1);
    String pageName = in.group(2);
    if (wikiName != null) {
      wikiName = wikiName.substring(0, wikiName.length() - 1);
    }
    return new LinkParts(in.group(), wikiName, pageName);
  }

}
