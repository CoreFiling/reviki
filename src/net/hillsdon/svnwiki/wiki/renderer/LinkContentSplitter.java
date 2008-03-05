package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.MatchResult;

public interface LinkContentSplitter {

  LinkParts split(MatchResult in);
  
}
