package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;

/**
 * Obviously this has security issues...
 * 
 * @author mth
 */
public class UnescapedHtmlNode extends AbstractRegexNode {

  public UnescapedHtmlNode() {
    super("(?s)\\[<html>\\](.*?)\\[</html>\\]");
  }

  public String handle(final PageReference page, final Matcher matcher) {
    return matcher.group(1);
  }

}
