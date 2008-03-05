package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.wiki.renderer.creole.AbstractRegexNode;

/**
 * Obviously this has security issues...
 * 
 * @author mth
 */
public class UnescapedHtmlNode extends AbstractRegexNode {

  public UnescapedHtmlNode(final boolean block) {
    super(block ? "(?s)(?:^|\\n)\\[<html>\\](.*?(^|\\n))\\[</html>\\]"
                : "(?s)\\[<html>\\](.*?)\\[</html>\\]");
  }

  public String handle(final PageReference page, final Matcher matcher) {
    return matcher.group(1);
  }

}
