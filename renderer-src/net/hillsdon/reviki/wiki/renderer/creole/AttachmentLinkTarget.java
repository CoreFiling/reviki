package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URI;
import java.net.URISyntaxException;

import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class AttachmentLinkTarget implements LinkTarget {
  private String _wiki;

  private String _pageName;

  private String _attachment;

  public AttachmentLinkTarget(final String wiki, final String pageName, final String attachment) {
    _wiki = wiki;
    _pageName = pageName;
    _attachment = attachment;
  }

  public String getPageName() {
    return _pageName;
  }

  public String getAttachment() {
    return _attachment;
  }

  public boolean exists(LinkResolutionContext linkResolutionContext) {
    return true;
  }
  
  public boolean isNoFollow(LinkResolutionContext linkResolutionContext) {
    return false;
  }
  
  public String getStyleClass(LinkResolutionContext linkResolutionContext) {
    return "attachment";
  }

  public String getURL(LinkResolutionContext resolver) throws UnknownWikiException, URISyntaxException {
    URI pageUri = resolver.resolve(_wiki, _pageName);
    return new URI(pageUri.getScheme(), pageUri.getUserInfo(), pageUri.getHost(), pageUri.getPort(), pageUri.getPath() + "/attachments/" + _attachment, null, null).toASCIIString();
  }

}
