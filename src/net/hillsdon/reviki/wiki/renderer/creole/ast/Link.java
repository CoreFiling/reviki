package net.hillsdon.reviki.wiki.renderer.creole.ast;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;

public class Link extends ASTNode {
  private LinkPartsHandler handler;

  private URLOutputFilter urlOutputFilter;

  private PageInfo page;

  private LinkParts parts;

  public Link(String target, String title, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    super("a");

    this.parts = (new CreoleLinkContentsSplitter()).split(target, title);
    this.page = page;
    this.urlOutputFilter = urlOutputFilter;
    this.handler = handler;
  }

  public String toXHTML() {
    if (parts.getText().startsWith("mailto:")) {
      return String.format("<a href='%s'>%s</a>", parts.getText(), Escape.html(parts.getText()));
    }
    try {
      return handler.handle(page, Escape.html(parts.getText()), parts, urlOutputFilter);
    }
    catch (Exception e) {
      return Escape.html(parts.getText());
    }
  }

}
