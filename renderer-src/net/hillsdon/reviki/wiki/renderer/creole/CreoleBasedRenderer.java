package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.wiki.MarkupRenderer;
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

  /**
   * Construct a renderer from just a resolution context.
   *
   * Simple anchors, simple images, no macros.
   */
  public CreoleBasedRenderer(final LinkResolutionContext resolver) {
    // Use the resolver's store.
    _pageStore = resolver.getPageStore();

    // Render links as links, and images as images.
    _linkHandler = new SimpleAnchors(resolver);
    _imageHandler = new SimpleImages(resolver);

    // We have no macros, either.
    _macros = Suppliers.ofInstance((List<Macro>) new LinkedList<Macro>());
  }

  @Override
  public final ASTNode parse(final PageInfo page) {
    _page = page;
    return CreoleRenderer.render(_pageStore, _page, _linkHandler, _imageHandler, _macros);
  }

  public LinkPartsHandler getLinkPartsHandler() {
    return _linkHandler;
  }
}
