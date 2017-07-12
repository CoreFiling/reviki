package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class DelegatingRenderer extends HtmlRenderer {

  private PageInfo _page;

  private final Map<SyntaxFormats, HtmlRenderer> _renderers;

  public DelegatingRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _renderers = new LinkedHashMap<SyntaxFormats, HtmlRenderer>();
    _renderers.put(SyntaxFormats.REVIKI, new RevikiRenderer(pageStore, linkHandler, imageHandler, macros));
    _renderers.put(SyntaxFormats.MARKDOWN, new MarkdownRenderer(pageStore, linkHandler, imageHandler, macros));
  }

  public DelegatingRenderer(final LinkResolutionContext resolver) {
    _renderers = new LinkedHashMap<SyntaxFormats, HtmlRenderer>();
    _renderers.put(SyntaxFormats.REVIKI, new RevikiRenderer(resolver));
    _renderers.put(SyntaxFormats.MARKDOWN, new MarkdownRenderer(resolver));
  }

  @Override
  public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
    _page = page;
    return getRenderer().parse(page);
  }

  @Override
  public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return getRenderer().render(ast, urlOutputFilter);
  }

  @Override
  public LinkPartsHandler getLinkPartsHandler() {
    return getRenderer().getLinkPartsHandler();
  }

  private HtmlRenderer getRenderer() {
    return _renderers.get(getSyntax());
  }

  private SyntaxFormats getSyntax() {
    if (_page != null && _page.getAttributes().containsKey("syntax")) {
      SyntaxFormats format = SyntaxFormats.fromValue(_page.getAttributes().get("syntax"));
      if (format != null) {
        return format;
      }
    }
    // TODO: Wiki default
    return SyntaxFormats.REVIKI;
  }

}