package net.hillsdon.svnwiki.wiki.renderer;

import java.util.regex.Matcher;

import net.hillsdon.svnwiki.vc.PageReference;

public abstract class AbstractLinkNode extends AbstractRegexNode {

  private final LinkContentSplitter _parser;
  private final LinkPartHandler _handler;

  public AbstractLinkNode(final String regex, final LinkContentSplitter parser, final LinkPartHandler handler) {
    super(regex);
    _parser = parser;
    _handler = handler;
  }

  public final String handle(final PageReference page, final Matcher matcher) {
    return _handler.handle(page, this, _parser.split(matcher));
  }

}
