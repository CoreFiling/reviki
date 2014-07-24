package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

public class Image extends ASTNode {
  private LinkPartsHandler _handler;

  private URLOutputFilter _urlOutputFilter;

  private PageInfo _page;

  private LinkParts _parts;

  public Image(final String target, final String title, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    super("img");

    _parts = (new CreoleLinkContentsSplitter()).split(target, title);
    _page = page;
    _urlOutputFilter = urlOutputFilter;
    _handler = handler;
  }

  @Override
  public String toXHTML() {
    try {
      return _handler.handle(_page, _parts.getText(), _parts, _urlOutputFilter);
    }
    catch (Exception e) {
      // TODO: Come up with a better way to handle this
      return "<img src=\"#\" alt=\"Bad Link\">";
    }
  }
}
