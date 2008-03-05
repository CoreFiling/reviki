package net.hillsdon.svnwiki.wiki.renderer.creole;

import java.util.regex.MatchResult;

public interface LinkContentSplitter {

  LinkParts split(MatchResult in);
  
}
