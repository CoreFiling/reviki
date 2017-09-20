package net.hillsdon.reviki.vc.impl;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Function;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SyntaxFormats;

public class PageInfoImpl extends PageReferenceImpl implements PageInfo {

  private final String _wiki;
  private final String _content;
  private final Map<String, String> _attributes;

  // For testing purposes.
  public PageInfoImpl(final String path) {
    this("", path, "", Collections.<String, String>emptyMap());
  }

  public PageInfoImpl(final String wiki, final String path, final String content, final Map<String, String> attributes) {
    super(path);
    _wiki = wiki;
    _content = content;
    _attributes = attributes;
  }

  @Override
  public String getWiki() {
    return _wiki;
  }

  @Override
  public String getContent() {
    return _content;
  }

  @Override
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  @Override
  public PageInfo withAlternativeContent(final String content) {
    return new PageInfoImpl(_wiki, super.getPath(), content, _attributes);
  }

  @Override
  public PageInfo withAlternativeAttributes(final Map<String, String> attributes) {
    return new PageInfoImpl(_wiki, super.getPath(), _content, attributes);
  }

  @Override
  public SyntaxFormats getSyntax(final Function<String, String> defaultSyntax) {
    String syntax = getAttributes().get("syntax");
    if (syntax != null) {
      SyntaxFormats format = SyntaxFormats.fromValue(syntax);
      if (format != null) {
        return format;
      }
    }
    if (defaultSyntax != null) {
      syntax = defaultSyntax.apply(super.getPath());
      if (syntax != null) {
        final SyntaxFormats format = SyntaxFormats.fromValue(syntax);
        if (format != null) {
          return format;
        }
      }
    }
    return SyntaxFormats.REVIKI;
  }
}
