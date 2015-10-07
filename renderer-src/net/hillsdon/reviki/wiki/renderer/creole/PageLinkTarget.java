package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;
import java.net.URISyntaxException;

import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public abstract class PageLinkTarget implements LinkTarget {

  public PageLinkTarget() {
    super();
  }

  public abstract boolean isLinkToCurrentWiki();

  protected abstract String getWiki(LinkResolutionContext resolver) throws UnknownWikiException;

  public abstract String getPageName();

  protected abstract String getRevision();

  protected abstract String getFragment();

  public boolean exists(SimplePageStore store) {
    try {
      return !isLinkToCurrentWiki() || store.exists(new PageReferenceImpl(getPageName()));
    }
    catch (PageStoreException e) {
      throw new RuntimeException(e);
    }
  }
  private boolean isDotAttachment(LinkResolutionContext resolver) {
    return getPageName().contains(".") && !exists(resolver.getPageStore());
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
    return isLinkToCurrentWiki() && !exists(resolver.getPageStore());
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

    final boolean exists = !isLinkToCurrentWiki() || exists(resolver.getPageStore());

    return exists ? "existing-page" : "new-page";
  }

  public String getURL(final LinkResolutionContext resolver) throws URISyntaxException, UnknownWikiException {
    if (isDotAttachment(resolver)) {
      return new AttachmentLinkTarget(getWiki(resolver), null, getPageName()).getURL(resolver);
    }
    URI uri = resolver.resolve(getWiki(resolver), getPageName(), getRevision());

    // uri could be an opaque URI (eg a URI with mailto: scheme)
    if (uri.isOpaque()) {
      uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), getFragment());
    }
    else {
      uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), getFragment());
    }

    return uri.toASCIIString();
  }
}
