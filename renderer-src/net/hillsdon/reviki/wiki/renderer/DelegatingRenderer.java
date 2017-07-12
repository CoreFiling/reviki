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
    return new DelegatingNode(getRenderer(page).parse(page), page);
  }

  @Override
  public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return getRenderer(getPage(ast)).render(getNode(getNode(ast)), urlOutputFilter);
  }

  private final class DelegatingNode extends ASTNode {
    private final ASTNode _node;
    private final PageInfo _page;

    public DelegatingNode(final ASTNode node, final PageInfo page) {
      _node = node;
      _page = page;
    }

    public ASTNode getNode() {
      return _node;
    }

    public PageInfo getPage() {
      return _page;
    }
  }

  @Override
  public LinkPartsHandler getLinkPartsHandler() {
    return getRenderer(null).getLinkPartsHandler();
  }

  private PageInfo getPage(final ASTNode ast) {
    if (ast instanceof DelegatingNode) {
      return ((DelegatingNode) ast).getPage();
    }
    return null;
  }

  private ASTNode getNode(final ASTNode ast) {
    if (ast instanceof DelegatingNode) {
      return ((DelegatingNode) ast).getNode();
    }
    return ast;
  }

  private HtmlRenderer getRenderer(final PageInfo page) {
    return _renderers.get(getSyntax(page));
  }

  private SyntaxFormats getSyntax(final PageInfo page) {
    if (page != null && page.getAttributes().containsKey("syntax")) {
      SyntaxFormats format = SyntaxFormats.fromValue(page.getAttributes().get("syntax"));
      if (format != null) {
        return format;
      }
    }
    // TODO: Wiki default
    return SyntaxFormats.REVIKI;
  }

}