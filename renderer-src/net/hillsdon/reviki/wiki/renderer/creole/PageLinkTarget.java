package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class PageLinkTarget implements LinkTarget {
  private String _wiki;

  private String _pageName;

  private String _revision;

  private String _fragment;

  public PageLinkTarget(final String wiki, final String pageName, final String revision, final String fragment) {
    _wiki = wiki;
    _pageName = pageName;
    _revision = revision;
    _fragment = fragment;
  }

  public PageLinkTarget(final String wiki, final String pageName, final String fragment) {
    this(wiki, pageName, null, fragment);
  }

  public String getWiki() {
    return _wiki;
  }

  public String getPageName() {
    return _pageName;
  }

  public String getFragment() {
    return _fragment;
  }

  public boolean exists(LinkResolutionContext linkResolutionContext) {
    try {
      return _wiki != null || linkResolutionContext.exists(new PageReferenceImpl(_pageName));
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  
  private boolean isDotAttachment(LinkResolutionContext resolver) {
    return _pageName.contains(".") && !exists(resolver);
  }

  public boolean isNoFollow(LinkResolutionContext resolver) {
    if (isDotAttachment(resolver)) {
      return new AttachmentLinkTarget(_wiki, null, _pageName).isNoFollow(resolver);
    }
    return _wiki == null && !exists(resolver);
  }

  public String getStyleClass(LinkResolutionContext resolver) {
    if (isDotAttachment(resolver)) {
      return new AttachmentLinkTarget(_wiki, null, _pageName).getStyleClass(resolver);
    }
    if (_wiki != null) {
      return "inter-wiki";
    }
    
    final boolean exists = _wiki != null || exists(resolver);

    return exists ? "existing-page" : "new-page";
  }

  public String getURL(final LinkResolutionContext resolver) throws URISyntaxException, UnknownWikiException {
    if (isDotAttachment(resolver)) {
      return new AttachmentLinkTarget(_wiki, null, _pageName).getURL(resolver);
    }
    URI uri = resolver.resolve(_wiki, _pageName, _revision);
    uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), _fragment);
    return uri.toASCIIString();
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
    PageLinkTarget other = (PageLinkTarget) obj;
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
