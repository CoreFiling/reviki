package net.hillsdon.reviki.wiki.renderer;

import java.util.List;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * Common abstract class for rendering directly from Creole.
 *
 * @author msw
 */
public abstract class CreoleBasedRenderer<T> extends MarkupRenderer<T> {

  protected final SimplePageStore _pageStore;

  protected final LinkPartsHandler _linkHandler;

  protected final LinkPartsHandler _imageHandler;

  protected final Supplier<List<Macro>> _macros;

  protected PageInfo _page;

  public CreoleBasedRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _pageStore = pageStore;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
    _macros = macros;
  }

  @Override
  public final ASTNode parse(final PageInfo page) {
    _page = page;
    return CreoleRenderer.render(_pageStore, _page, _linkHandler, _imageHandler, _macros);
  }
}
