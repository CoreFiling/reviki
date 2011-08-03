package net.hillsdon.reviki.vc.impl;

import java.util.Collections;
import java.util.Map;

import net.hillsdon.reviki.vc.PageInfo;

public class PageInfoImpl extends PageReferenceImpl implements PageInfo {

  private final String _wiki;
  private final String _content;
  private final Map<String, String> _attributes;

  // For testing purposes.
  public PageInfoImpl(String path) {
    this("", path, "", Collections.<String, String>emptyMap());
  }

  public PageInfoImpl(String wiki, String path, String content, Map<String, String> attributes) {
    super(path);
    _wiki = wiki;
    _content = content;
    _attributes = attributes;
  }

  public String getWiki() {
    return _wiki;
  }

  public String getContent() {
    return _content;
  }

  public Map<String, String> getAttributes() {
    return _attributes;
  }

  public PageInfo withAlternativeContent(String content) {
    return new PageInfoImpl(_wiki, super.getPath(), content, _attributes);
  }

  public PageInfo withAlternativeAttributes(Map<String, String> attributes) {
    return new PageInfoImpl(_wiki, super.getPath(), _content, attributes);
  }

}
