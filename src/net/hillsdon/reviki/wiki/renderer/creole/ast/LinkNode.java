package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.links.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.links.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.links.LinkPartsHandler;

/**
 * Abstract node type for things rendered using link handlers.
 *
 * @author msw
 */
public abstract class LinkNode extends TaggedNode {
  private final LinkPartsHandler _handler;

  private final URLOutputFilter _urlOutputFilter;

  private final PageInfo _page;

  private final LinkParts _parts;

  private final String _title;

  private final String _target;

  public LinkNode(final String tag, final String target, final String title, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    super(tag);

    _title = title;
    _target = target;
    _parts = CreoleLinkContentsSplitter.split(target, title);
    _page = page;
    _urlOutputFilter = urlOutputFilter;
    _handler = handler;
  }

  /**
   * Get the split link.
   */
  public LinkParts getParts() {
    return _parts;
  }

  @Override
  public String toXHTML() {
    try {
      return _handler.handle(_page, Escape.html(_parts.getText()), _parts, _urlOutputFilter);
    }
    catch (Exception e) {
      // Special case: render mailto: as a link if it didn't get interwiki'd
      if (_target.startsWith("mailto:") && tag().equals("a")) {
        return String.format("<a href='%s'>%s</a>", _target, Escape.html(_title));
      }
      else {
        return Escape.html(_parts.getText());
      }
    }
  }
}
