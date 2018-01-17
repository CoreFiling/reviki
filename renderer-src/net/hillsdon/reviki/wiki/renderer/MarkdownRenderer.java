package net.hillsdon.reviki.wiki.renderer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleLinkContentsSplitter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.LinkTarget;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleAnchors;
import net.hillsdon.reviki.wiki.renderer.creole.SimpleImages;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Raw;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

public class MarkdownRenderer extends HtmlRenderer {

  private static final Log LOG = LogFactory.getLog(MarkdownRenderer.class);

  private static final Pattern MACRO_REGEX = Pattern.compile("\\[([^:\\]]+)(?::([^\\]]*))?\\]");

  private final SimplePageStore _pageStore;

  private final LinkPartsHandler _linkHandler;

  private final LinkPartsHandler _imageHandler;

  private final Supplier<List<Macro>> _macros;

  private final List<Extension> _extensions = ImmutableList.of(
      TablesExtension.create(),
      StrikethroughExtension.create(),
      AutolinkExtension.create(),
      MultilineQuoteExtension.create());

  public MarkdownRenderer(final SimplePageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _pageStore = pageStore;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
    _macros = macros;
  }

  public MarkdownRenderer(final LinkResolutionContext resolver) {
    _pageStore = resolver.getPageStore();
    _linkHandler = new SimpleAnchors(resolver);
    _imageHandler = new SimpleImages(resolver);
    _macros = Suppliers.ofInstance((List<Macro>) new LinkedList<Macro>());
  }

  @Override
  public String getContentType(final PageInfo page) {
    return "text/html; charset=utf-8";
  }

	@Override
	public ASTNode parse(final PageInfo page) throws IOException, PageStoreException {
	  return new Raw("");
	}

  @Override
	public String render(final PageInfo page, final ASTNode ast, final URLOutputFilter urlOutputFilter) throws IOException, PageStoreException {
    Parser parser = Parser.builder().extensions(_extensions).build();
    Node document = parser.parse(page.getContent());
    document.accept(new MarkdownVisitor(page, urlOutputFilter));
    return org.commonmark.renderer.html.HtmlRenderer.builder()
          .attributeProviderFactory(MarkdownAttributeProvider.factory())
          .extensions(_extensions)
        .build()
        .render(document);
	}

  private static class MarkdownAttributeProvider implements AttributeProvider {
    private static final Set<String> WIKI_CONTENT_TAGS = ImmutableSet.of("table", "tr", "th", "td");

    @Override
    public void setAttributes(final Node node, final String tagName, final Map<String, String> attributes) {
      if (WIKI_CONTENT_TAGS.contains(tagName.toLowerCase(Locale.ENGLISH))) {
        attributes.put("class", "wiki-content");
      }
      if ("table".equalsIgnoreCase(tagName)) {
        attributes.put("style", "margin: 10px 0px");
      }
    }

    public static AttributeProviderFactory factory() {
      return new AttributeProviderFactory() {
        @Override
        public AttributeProvider create(final AttributeProviderContext context) {
          return new MarkdownAttributeProvider();
        }
      };
    }
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
      int searchStart = 0;
      while (regex.find(searchStart)) {
        Optional<? extends Node> newNode = handleMacro(regex.group(1), regex.group(2));
        if (newNode.isPresent()) {
          if (regex.start() > start) {
            textNode.insertBefore(new Text(textNode.getLiteral().substring(start, regex.start())));
          }

          textNode.insertBefore(newNode.get());
          start = regex.end();
        }
        searchStart = regex.end();
      }
      if (start == textNode.getLiteral().length()) {
        textNode.unlink();
      }
      if (start > 0) {
        textNode.setLiteral(textNode.getLiteral().substring(start));
      }
    }

    private Optional<? extends Node> handleMacro(final String macroName, final String macroArgs) {
      try {
        for (Macro macro : _macros.get()) {
          if (macro.getName().equals(macroName)) {
            String content = macro.handle(_page, macroArgs);

            switch (macro.getResultFormat()) {
              case XHTML:
                return Optional.of(htmlNode(content));
              case WIKI:
                RevikiRenderer reviki = new RevikiRenderer(_pageStore, _linkHandler, _imageHandler, _macros);
                Optional<String> html = reviki.render(content, _urlOutputFilter);
                return html.transform(new Function<String, Node>() {
                  @Override
                  public Node apply(final String t) {
                    return htmlNode(t);
                  }
                });
              default:
                return Optional.of(new Text(content));
            }
          }
        }
      }
      catch (Exception e) {
        LOG.error("Error handling macro on: " + _page.getPath(), e);
      }
      return Optional.absent();
    }

    private HtmlInline htmlNode(final String content) {
      final HtmlInline node = new HtmlInline();
      node.setLiteral(content.replaceFirst("^<p " + RevikiRenderer.CSS_CLASS_ATTR + ">(.*)</p>$", "$1"));
      return node;
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

  @Override
  public LinkPartsHandler getLinkPartsHandler() {
    return _linkHandler;
  }
}
