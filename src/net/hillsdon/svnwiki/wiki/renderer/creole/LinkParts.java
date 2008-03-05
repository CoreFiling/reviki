package net.hillsdon.svnwiki.wiki.renderer.creole;

public class LinkParts {
  private final String _text;
  private final String _wiki;
  private final String _refd;
  public LinkParts(final String text, final String wiki, final String refd) {
    _text = text;
    _wiki = wiki;
    _refd = refd;
  }
  public String getText() {
    return _text;
  }
  public String getWiki() {
    return _wiki;
  }
  public String getRefd() {
    return _refd;
  }
  public boolean isURL() {
    return _wiki == null && getRefd().matches("\\p{L}+?:.*");
  }
}