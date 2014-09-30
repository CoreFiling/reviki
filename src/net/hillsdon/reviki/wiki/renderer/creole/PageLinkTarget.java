package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;
import java.net.URISyntaxException;

import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public abstract class PageLinkTarget implements LinkTarget {

  public PageLinkTarget() {
    super();
  }

  public abstract boolean isLinkToCurrentWiki();
  
  protected abstract String getWiki(LinkResolutionContext linkResolutionContext) throws UnknownWikiException;

  public abstract String getPageName();

  protected abstract String getRevision();

  protected abstract String getFragment();

  public boolean exists(LinkResolutionContext linkResolutionContext) {
    try {
      return !isLinkToCurrentWiki() || linkResolutionContext.exists(new PageReferenceImpl(getPageName()));
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  private boolean isDotAttachment(LinkResolutionContext resolver) {
    return getPageName().contains(".") && !exists(resolver);
  }

  public boolean isNoFollow(LinkResolutionContext resolver) {
    if (isDotAttachment(resolver)) {
      try {
        return new AttachmentLinkTarget(getWiki(resolver), null, getPageName()).isNoFollow(resolver);
      }
      catch (UnknownWikiException ex) {
        return true; 
      }
    }
    return isLinkToCurrentWiki() && !exists(resolver);
  }

  public String getStyleClass(LinkResolutionContext resolver) {
    if (isDotAttachment(resolver)) {
      try {
        return new AttachmentLinkTarget(getWiki(resolver), null, getPageName()).getStyleClass(resolver);
      }
      catch (UnknownWikiException ex) {
        // Then it won't be a link to the current wiki, so use the inter-wiki style below
      }
    }
    if (!isLinkToCurrentWiki()) {
      return "inter-wiki";
    }
    
    final boolean exists = !isLinkToCurrentWiki() || exists(resolver);

    return exists ? "existing-page" : "new-page";
  }

  public String getURL(final LinkResolutionContext resolver) throws URISyntaxException, UnknownWikiException {
    if (isDotAttachment(resolver)) {
      return new AttachmentLinkTarget(getWiki(resolver), null, getPageName()).getURL(resolver);
    }
    URI uri = resolver.resolve(getWiki(resolver), getPageName(), getRevision());
    uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), getFragment());
    return uri.toASCIIString();
  }
}