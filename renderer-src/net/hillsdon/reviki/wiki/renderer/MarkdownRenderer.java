package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.LinkTarget;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleAnchors;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Raw;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MarkdownRenderer extends MarkupRenderer<String> {

  private static final Pattern MACRO_REGEX = Pattern.compile("\\[(search):([^\\]]*)\\]");

  private Node _document;

  private final LinkPartsHandler _linkHandler;
  private PageInfo _page;

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
	  _document = parser.parse(page.getContent());
	  _page = page;
	  return new Raw("");
	}

  @Override
	public String render(final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    _document.accept(new MarkdownVisitor(_page, urlOutputFilter));
    return HtmlRenderer.builder().build().render(_document);
	}

  public LinkPartsHandler getLinkPartsHandler() {
    return _linkHandler;
  }

  private class MarkdownVisitor extends AbstractVisitor {

    private final PageInfo _page;
    private final URLOutputFilter _urlOutputFilter;

    public MarkdownVisitor(final PageInfo page, final URLOutputFilter urlOutputFilter) {
      _page = page;
      _urlOutputFilter = urlOutputFilter;
    }

    @Override
    public void visit(final Image image) {
      String url = resolveUrlToPage(image.getDestination());
      if (url == null) {
        image.insertBefore(new Text(image.getTitle() == null ? image.getDestination() : image.getTitle()));
        image.unlink();
      }
      else {
        image.setDestination(url);
      }
    }

    @Override
    public void visit(final Link link) {
      String url = resolveUrlToPage(link.getDestination());
      if (url == null) {
        link.insertBefore(new Text(link.getTitle() == null ? link.getDestination() : link.getTitle()));
        link.unlink();
      }
      else {
        link.setDestination(url);
      }
    }

    @Override
    public void visit(final Text textNode) {
      Matcher regex = MACRO_REGEX.matcher(textNode.getLiteral());
      int start = 0;
      while (regex.find(start)) {
        if (regex.start() > start) {
          textNode.insertBefore(new Text(textNode.getLiteral().substring(start, regex.start())));
        }

        String macroType = regex.group(1);
        String macroArgument = regex.group(2);

        StrongEmphasis strong = new StrongEmphasis();
        strong.appendChild(new Text("Warning: macros have not been implemented yet"));
        textNode.insertBefore(strong);
        // TODO: handle macros

        start = regex.end();
      }
      if (start == textNode.getLiteral().length()) {
        textNode.unlink();
      }
      if (start > 0) {
        textNode.setLiteral(textNode.getLiteral().substring(start));
      }
    }

    private String resolveUrlToPage(final String url) {
      LinkTarget target = CreoleLinkContentsSplitter.split(url, null).getTarget();
      try {
        return _urlOutputFilter.filterURL(target.getURL(_linkHandler.getContext().derive(_page)));
      }
      catch (URISyntaxException e) {
        // Fall through to show a dead link
      }
      catch (UnknownWikiException e) {
        // Fall through to show a dead link
      }
      return null;
    }

  }
}
