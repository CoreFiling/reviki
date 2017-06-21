package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class HtmlRenderer extends MarkupRenderer<String> {

  private PageInfo _page;
  private final RevikiRenderer _reviki;
  private final MarkdownRenderer _markdown;

  public HtmlRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _reviki = new RevikiRenderer(pageStore, linkHandler, imageHandler, macros);
    _markdown = new MarkdownRenderer(pageStore, linkHandler, imageHandler, macros);
  }

  public HtmlRenderer(final LinkResolutionContext resolver) {
    _reviki = new RevikiRenderer(resolver);
    _markdown = new MarkdownRenderer(resolver);
  }

  @Override
  public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
    _page = page;
    return isMarkdown() ? _markdown.parse(page) : _reviki.parse(page);
  }

  @Override
  public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return isMarkdown() ? _markdown.render(ast, urlOutputFilter) : _reviki.render(ast, urlOutputFilter);
  }

  public LinkPartsHandler getLinkPartsHandler() {
    return isMarkdown() ? _markdown.getLinkPartsHandler() : _reviki.getLinkPartsHandler();
  }

  private boolean isMarkdown() {
    return _page != null && _page.getAttributes().containsKey("markdown");
  }

}