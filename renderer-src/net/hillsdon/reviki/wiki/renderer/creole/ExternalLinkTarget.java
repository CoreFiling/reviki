package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;

import net.hillsdon.reviki.vc.SimplePageStore;

public class ExternalLinkTarget implements LinkTarget {
  
  private URI _uri;
  
  public ExternalLinkTarget(URI uri) {
    _uri = uri;
  }
  
  public URI getURI() {
    return _uri;
  }

  public boolean exists(final SimplePageStore resolver) {
    return true;
  }
  
  public boolean isNoFollow(final LinkResolutionContext resolver) {
    return false;
  }

  public String getStyleClass(final LinkResolutionContext resolver) {
    return "external";
  }

  public String getURL(final LinkResolutionContext resolver) {
    return _uri.toASCIIString();
  }

  @Override
  public int hashCode() {
    return _uri.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ExternalLinkTarget other = (ExternalLinkTarget) obj;
    if (_uri == null) {
      if (other._uri != null)
        return false;
    }
    else if (!_uri.equals(other._uri))
      return false;
    return true;
  }

  public String toString() {
    return getClass().getSimpleName() + "=" +_uri.toString();
  }
}
