package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Raw;

/**
 * A basic renderer which just turns its input into a stream. This exploits the
 * fact that {@link #parse(PageInfo)} is always called before
 * {@link #render(ASTNode, URLOutputFilter)}.
 *
 * @author msw
 */
public class RawRenderer extends MarkupRenderer<InputStream> {
  private PageInfo _page;

  @Override
  public ASTNode parse(final PageInfo page) {
    _page = page;
    return new Raw(page.getContent());
  }

  @Override
  public InputStream render(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    try {
      return new ByteArrayInputStream(_page.getContent().getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e) {
      return new ByteArrayInputStream(_page.getContent().getBytes());
    }
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
