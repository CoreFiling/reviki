package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleAnchors;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Raw;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MarkdownRenderer extends MarkupRenderer<String> {

  private String _html;
  private final LinkPartsHandler _linkHandler;

  public MarkdownRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _linkHandler = linkHandler;
  }

  public MarkdownRenderer(final LinkResolutionContext resolver) {
    _linkHandler = new SimpleAnchors(resolver);
  }

  @Override
  public String getContentType() {
    return "text/html; charset=utf-8";
  }

	@Override
	public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
	  Parser parser = Parser.builder().build();
	  Node document = parser.parse(page.getContent());
	  _html = HtmlRenderer.builder().build().render(document);
	  return new Raw(_html);
	}

  @Override
	public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    return _html;
	}

  public LinkPartsHandler getLinkPartsHandler() {
    return _linkHandler;
  }

}
