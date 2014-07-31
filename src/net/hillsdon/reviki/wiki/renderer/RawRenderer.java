package net.hillsdon.reviki.wiki.renderer;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;

public class RawRenderer extends MarkupRenderer<String> {
  private PageInfo _page;

  @Override
  public ASTNode render(final PageInfo page) {
    _page = page;
    return CreoleRenderer.render(page, null, null);
  }

  @Override
  public String build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    return _page.getContent();
  }

  @Override
  public String getContentType() {
    // This is a cludge. We should represent 'special' pages better.
    if (_page.getPath().equals("ConfigCss")) {
      return "text/css";
    }
    else {
      return "text/plain";
    }
  }
}
