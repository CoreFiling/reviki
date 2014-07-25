package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

public class Link extends TaggedNode {
  private LinkPartsHandler _handler;

  private URLOutputFilter _urlOutputFilter;

  private PageInfo _page;

  private LinkParts _parts;

  private String _title;

  private String _target;

  public Link(final String target, final String title, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    super("a");

    _title = title;
    _target = target;
    _parts = (new CreoleLinkContentsSplitter()).split(target, title);
    _page = page;
    _urlOutputFilter = urlOutputFilter;
    _handler = handler;
  }

  @Override
  public String toXHTML() {
    try {
      return _handler.handle(_page, Escape.html(_parts.getText()), _parts, _urlOutputFilter);
    }
    catch (Exception e) {

      if (_target.startsWith("mailto:")) {
        return String.format("<a href='%s'>%s</a>", _target, Escape.html(_title));
      }
      else {
        return Escape.html(_parts.getText());
      }
    }
  }

}
