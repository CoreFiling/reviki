package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;

public class LinkNode extends AbstractRegexNode {

  private final LinkContentSplitter _parser;
  private final LinkPartsHandler _handler;

  public LinkNode(final String regex, final LinkContentSplitter parser, final LinkPartsHandler handler) {
    super(regex);
    _parser = parser;
    _handler = handler;
  }

  public final String handle(final PageReference page, final Matcher matcher) {
    return _handler.handle(page, this, _parser.split(matcher));
  }

}
