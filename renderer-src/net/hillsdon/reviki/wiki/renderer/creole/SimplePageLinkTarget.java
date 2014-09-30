package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.Arrays;

public class SimplePageLinkTarget extends PageLinkTarget {
  private String _wiki;

  private String _pageName;

  private String _revision;

  private String _fragment;

  public SimplePageLinkTarget(final String wiki, final String pageName, final String revision, final String fragment) {
    _wiki = wiki;
    _pageName = pageName;
    _revision = revision;
    _fragment = fragment;
  }

  public boolean isLinkToCurrentWiki() {
    return _wiki == null;
  }

  protected String getWiki(LinkResolutionContext resolver) {
    return _wiki;
  }

  public String getPageName() {
    return _pageName;
  }

  public String getFragment() {
    return _fragment;
  }

  public String getRevision() {
    return _revision;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_fragment == null) ? 0 : _fragment.hashCode());
    result = prime * result + ((_pageName == null) ? 0 : _pageName.hashCode());
    result = prime * result + ((_wiki == null) ? 0 : _wiki.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SimplePageLinkTarget other = (SimplePageLinkTarget) obj;
    if (_fragment == null) {
      if (other._fragment != null)
        return false;
    }
    else if (!_fragment.equals(other._fragment))
      return false;
    if (_pageName == null) {
      if (other._pageName != null)
        return false;
    }
    else if (!_pageName.equals(other._pageName))
      return false;
    if (_wiki == null) {
      if (other._wiki != null)
        return false;
    }
    else if (!_wiki.equals(other._wiki))
      return false;
    return true;
  }

  public String toString() {
    return getClass().getSimpleName() + Arrays.asList(_wiki, _pageName, _fragment).toString();
  }
}
