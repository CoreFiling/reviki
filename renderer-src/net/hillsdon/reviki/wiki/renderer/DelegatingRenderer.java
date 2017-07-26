package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.vc.SyntaxFormats;
import net.hillsdon.reviki.vc.impl.AutoPropertiesApplier;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class DelegatingRenderer extends HtmlRenderer {

  private final Map<SyntaxFormats, HtmlRenderer> _renderers;
  private final AutoPropertiesApplier _propsApplier;

  public DelegatingRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros, final AutoPropertiesApplier propsApplier) {
    _propsApplier = propsApplier;
    _renderers = new LinkedHashMap<SyntaxFormats, HtmlRenderer>();
    _renderers.put(SyntaxFormats.REVIKI, new RevikiRenderer(pageStore, linkHandler, imageHandler, macros));
    _renderers.put(SyntaxFormats.MARKDOWN, new MarkdownRenderer(pageStore, linkHandler, imageHandler, macros));
  }

  public DelegatingRenderer(final LinkResolutionContext resolver, final AutoPropertiesApplier propsApplier) {
    _propsApplier = propsApplier;
    _renderers = new LinkedHashMap<SyntaxFormats, HtmlRenderer>();
    _renderers.put(SyntaxFormats.REVIKI, new RevikiRenderer(resolver));
    _renderers.put(SyntaxFormats.MARKDOWN, new MarkdownRenderer(resolver));
  }

  @Override
  public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
    return getRenderer(page).parse(page);
  }

  @Override
  public String render(final PageInfo page, final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return getRenderer(page).render(page, ast, urlOutputFilter);
  }

  @Override
  public LinkPartsHandler getLinkPartsHandler() {
    return getRenderer(null).getLinkPartsHandler();
  }

  private HtmlRenderer getRenderer(final PageInfo page) {
    return _renderers.get(getSyntax(page));
  }

  private SyntaxFormats getSyntax(final PageInfo page) {
    if (page != null) {
      return page.getSyntax(_propsApplier);
    }
    return SyntaxFormats.REVIKI;
  }

}