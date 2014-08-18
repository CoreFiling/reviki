package net.hillsdon.reviki.wiki.renderer.creole;

import java.net.URISyntaxException;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.text.WikiWordUtils;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;

public class SimpleLinkHandler implements LinkPartsHandler {

  public static final String IMAGE = "<img %sclass='%s' src='%s' alt='%s' />";

  public static final String ANCHOR = "<a %sclass='%s' href='%s'>%s</a>";

  private final String _fmat;

  private final LinkResolutionContext _context;
  
  public SimpleLinkHandler(final String fmat, final LinkResolutionContext context) {
    _fmat = fmat;
    _context = context;
  }

  public String handle(final PageReference page, final String xhtmlContent, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    LinkResolutionContext resolver = _context.derive(page);
    String noFollow = parts.isNoFollow(resolver) ? "rel='nofollow' " : "";
    String clazz = parts.getStyleClass(resolver);

    if (isAcronymNotLink(page, parts)) {
      return parts.getText();
    }

    String url = handle(page, parts, urlOutputFilter);
    return String.format(_fmat, noFollow, Escape.html(clazz), url, xhtmlContent);
  }

  public String handle(final PageReference page, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
    LinkResolutionContext resolver = _context.derive(page);
    String url = parts.getURL(resolver);
    return urlOutputFilter.filterURL(url);
  }

  public LinkResolutionContext getContext() {
    return _context;
  }

  public boolean isAcronymNotLink(PageReference page, LinkParts parts) {
    return WikiWordUtils.isAcronym(parts.getText());
  }

}
