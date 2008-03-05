package net.hillsdon.svnwiki.wiki.renderer.creole;

import java.util.regex.MatchResult;

public class CreoleLinkContentsSplitter implements LinkContentSplitter {

  public LinkParts split(final MatchResult match) {
    String in = match.group(1);
    // [[wiki:PageName|Show this text]]
    // [[http://somewhere|Show this text]], so we have an ambiguity here.
    int pipeIndex = in.lastIndexOf("|");
    int colonIndex = in.indexOf(":");
    
    String text = pipeIndex == -1 ? in : in.substring(pipeIndex + 1);
    String wiki = colonIndex == -1 ? null : in.substring(0, colonIndex);
    String refd = in.substring(colonIndex + 1, pipeIndex == -1 ? in.length() : pipeIndex);
    if (refd.startsWith("/")) {
      refd = wiki + ":" + refd;
      wiki = null;
    }
    return new LinkParts(text, wiki, refd);
  }

}
