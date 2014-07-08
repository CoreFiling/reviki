package net.hillsdon.reviki.wiki.renderer.creole.parser.ast.result;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.result.LeafResultNode;

public class RenderedLink extends LeafResultNode {
  private LinkPartsHandler handler;

  private URLOutputFilter urlOutputFilter;

  private PageInfo page;

  private RenderNode renderer;

  private LinkParts parts;

  public RenderedLink(final PageInfo page, final RenderNode renderer, final LinkParts parts, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    this.parts = parts;
    this.page = page;
    this.renderer = renderer;
    this.urlOutputFilter = urlOutputFilter;
    this.handler = handler;
  }

  public String toXHTML() {
    try {
      //return handler.handle(page, renderer, parts, urlOutputFilter);
      // TODO: Handle links properly
      return "<a href=\"#\">" + parts.getText() + "</a>";
    }
    catch (Exception e) {
      // TODO: Come up with a better way to handle this
      return "<a href=\"#\">Bad Link</a>";
    }
  }

}
