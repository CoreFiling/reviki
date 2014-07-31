package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;

public class WrappedStreamRenderer extends MarkupRenderer<InputStream> {
  /** The wrapped renderer. */
  private final MarkupRenderer<String> _renderer;

  /**
   * @param wraps The string renderer to produce a stream from.
   */
  public WrappedStreamRenderer(MarkupRenderer<String> wraps) {
    _renderer = wraps;
  }

  @Override
  public ASTNode render(PageInfo page) throws IOException, PageStoreException {
    return _renderer.render(page);
  }

  @Override
  public InputStream build(ASTNode ast, URLOutputFilter urlOutputFilter) {
    String rendered = _renderer.build(ast, urlOutputFilter);
    return new ByteArrayInputStream(rendered.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String getContentType() {
    return _renderer.getContentType();
  }
}
