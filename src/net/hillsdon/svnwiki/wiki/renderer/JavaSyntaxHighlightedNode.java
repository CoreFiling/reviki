package net.hillsdon.svnwiki.wiki.renderer;

import java.io.IOException;
import java.util.regex.Matcher;

import net.hillsdon.svnwiki.text.Escape;
import net.hillsdon.svnwiki.vc.PageReference;

import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

/**
 * Syntax formatting for java as in vqwiki.
 * 
 * A general syntax needs some thought.
 * 
 * @author mth
 */
public class JavaSyntaxHighlightedNode extends AbstractRegexNode {

  public JavaSyntaxHighlightedNode() {
    super("(?s)\\[<java>\\](.*?)\\[</java>\\]");
  }

  public String handle(final PageReference page, final Matcher matcher) {
    String content = matcher.group(1);
    try {
      return XhtmlRendererFactory.getRenderer(XhtmlRendererFactory.JAVA).highlight("", content, "UTF-8", true);
    }
    catch (IOException e) {
      return "<pre>" + Escape.html(content) + "</pre>";
    }
  }

}
