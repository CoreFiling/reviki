package net.hillsdon.svnwiki.wiki.renderer;

import static net.hillsdon.svnwiki.text.WikiWordUtils.isWikiWord;

import java.util.regex.Matcher;

/**
 * Links like c2:WikiPage and WikiPage but very generous as to
 * what counts as a wiki word.
 * 
 * @author mth
 */
public class CustomWikiLinkNode extends LinkNode {
  
  public CustomWikiLinkNode(final LinkPartsHandler handler) {
    super("(\\p{Alnum}+:)?(\\p{Alnum}+)", new CustomLinkContentSplitter(), handler);
  }

  @Override
  protected boolean confirmMatchFind(final Matcher matcher) {
    do {
      String wikiName = matcher.group(1);
      String pageName = matcher.group(2);
      if (wikiName != null || isWikiWord(pageName)) {
        return true;
      }
    } while (matcher.find());
    return false;
  }

}
