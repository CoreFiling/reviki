package net.hillsdon.reviki.wiki.renderer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Supplier;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * A renderer for Docbook. Rather than an XML document, this renders to an input
 * stream for easy integration with the http output. The document (and string
 * serialisation) can be accessed with the
 * {@link #buildString(ASTNode, URLOutputFilter)} and
 * {@link #buildDocument(ASTNode, URLOutputFilter)} methods.
 *
 * @author msw
 */
public class DocbookRenderer extends MarkupRenderer<InputStream> {
  private final PageStore _pageStore;

  private final LinkPartsHandler _linkHandler;

  private final LinkPartsHandler _imageHandler;

  private final Supplier<List<Macro>> _macros;

  public DocbookRenderer(final PageStore pageStore, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler, final Supplier<List<Macro>> macros) {
    _pageStore = pageStore;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
    _macros = macros;
  }

  @Override
  public ASTNode render(final PageInfo page) {
    return CreoleRenderer.render(_pageStore, page, _linkHandler, _imageHandler, _macros);
  }

  @Override
  public InputStream build(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    String out = buildString(ast, urlOutputFilter);
    return new ByteArrayInputStream(out.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String getContentType() {
    return "text/xml; charset=utf-8";
  }

  public String buildString(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    String out;
    try {
      Document doc = buildDocument(ast, urlOutputFilter);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));
      out = writer.getBuffer().toString();
    }
    catch (Exception e) {
      out = "error: " + e;
    }

    return out;
  }

  public Document buildDocument(final ASTNode ast, final URLOutputFilter urlOutputFilter) {
    Document document;
    try {
      document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      document.setXmlVersion("1.0");
    }
    catch (ParserConfigurationException e) {
      // Not nice, but what can we do?
      throw new RuntimeException(e);
    }

    DocbookVisitor renderer = new DocbookVisitor(document);
    renderer.setUrlOutputFilter(urlOutputFilter);

    // The renderer builds the root element, in this case.
    document.appendChild(renderer.visit(ast).get(0));

    return document;
  }

  private final class DocbookVisitor extends ASTRenderer<List<Node>> {
    private final Document _document;

    public DocbookVisitor(final Document document) {
      super(new ArrayList<Node>());
      _document = document;
    }

    @Override
    protected List<Node> combine(final List<Node> x1, final List<Node> x2) {
      List<Node> combined = new ArrayList<Node>();
      combined.addAll(x1);
      combined.addAll(x2);
      return combined;
    }

    @Override
    public List<Node> visitPage(final Page node) {
      Element article = _document.createElement("article");
      article.setAttribute("xmlns", "http://docbook.org/ns/docbook");
      article.setAttribute("xmlns:xl", "http://www.w3.org/1999/xlink");
      article.setAttribute("version", "5.0");
      article.setAttribute("xml:lang", "en");

      // Stack of sections and subsections. Head of stack is what we're
      // currently rendering to.
      Stack<Element> sections = new Stack<Element>();

      // Stack of heading levels. Head of stack is the last heading we saw.
      // Seeing a smaller heading results in a new subsection being created,
      // seeing a larger heading results in sections being popped and saved to
      // the document/parent sections.
      //
      // Invariant: sections.size() == levels.size()
      //
      // There may be one more section than heading because the document might
      // not start off with a heading.
      Stack<Integer> levels = new Stack<Integer>();

      for (ASTNode child : node.getChildren()) {
        // Upon hitting a heading, we need to start a new section.
        if (child instanceof Heading) {
          // If there are sections around at the moment, we may need to
          // rearrange the document: we compare with the current heading:
          // popping things off the stack and merging them into their parent
          // sections if it's bigger.
          Integer hdr = new Integer(((Heading) child).getLevel());

          while (!levels.isEmpty() && levels.peek() >= hdr) {
            Element sect = sections.pop();
            levels.pop();

            Element parent = sections.isEmpty() ? article : sections.peek();
            parent.appendChild(sect);
          }

          // Then we finally make a new section.
          sections.push(_document.createElement("section"));
          levels.push(hdr);
        }

        // The document might not start off with a heading, in which case we
        // pretend there was a level 1 heading.
        if (sections.empty()) {
          sections.push(_document.createElement("section"));
          levels.push(new Integer(1));
        }

        // Add everything to the current section.
        for (Node sibling : visit(child)) {
          sections.peek().appendChild(sibling);
        }
      }

      // Save the last subsection stack.
      while (!sections.isEmpty()) {
        Element sect = sections.pop();
        Element parent = sections.isEmpty() ? article : sections.peek();
        parent.appendChild(sect);
      }

      // And we're done.
      return singleton(article);
    }

    /**
     * Helper function: build a node from an element type name and an ASTNode
     * containing children.
     */
    public Element wraps(final String element, final ASTNode node) {
      return (Element) build(_document.createElement(element), visitASTNode(node)).get(0);
    }

    /**
     * Helper function: build a node list from an element and some children.
     */
    public List<Node> build(final Element container, final List<Node> siblings) {
      for (Node sibling : siblings) {
        container.appendChild(sibling);
      }

      return singleton(container);
    }

    /**
     * Helper function: build a singleton list.
     */
    public List<Node> singleton(final Node n) {
      List<Node> out = new ArrayList<Node>();
      out.add(n);
      return out;
    }

    @Override
    public List<Node> visitBold(final Bold node) {
      Element strong = _document.createElement("emphasis");
      strong.setAttribute("role", "bold");
      return build(strong, visitASTNode(node));
    }

    @Override
    public List<Node> visitCode(final Code node) {
      Element out = _document.createElement("programlisting");
      out.setAttribute("language", "c++");

      if (node.getLanguage().isPresent()) {
        out.setAttribute("language", node.getLanguage().get().toString());
      }

      out.appendChild(_document.createCDATASection(node.getText()));

      return singleton(out);
    }

    @Override
    public List<Node> visitHeading(final Heading node) {
      Element out = _document.createElement("info");
      Element title = wraps("title", node);
      out.appendChild(title);

      return singleton(out);
    }

    @Override
    public List<Node> visitHorizontalRule(final HorizontalRule node) {
      Element out = _document.createElement("bridgehead");
      out.setAttribute("role", "separator");
      return singleton(out);
    }

    @Override
    public List<Node> renderImage(final String target, final String title, final Image node) {
      Element out = _document.createElement("imageobject");

      // Header
      Element info = _document.createElement("info");
      Element etitle = _document.createElement("title");
      etitle.appendChild(_document.createTextNode(title));
      info.appendChild(etitle);

      // Image data
      Element imagedata = _document.createElement("imagedata");
      imagedata.setAttribute("fileref", target);

      out.appendChild(info);
      out.appendChild(imagedata);

      return singleton(out);
    }

    @Override
    public List<Node> visitInlineCode(final InlineCode node) {
      Element out = _document.createElement("code");
      out.setAttribute("language", "c++");

      if (node.getLanguage().isPresent()) {
        out.setAttribute("language", node.getLanguage().get().toString());
      }

      out.appendChild(_document.createTextNode(node.getText()));

      return singleton(out);
    }

    @Override
    public List<Node> visitItalic(final Italic node) {
      return singleton(wraps("emphasis", node));
    }

    @Override
    public List<Node> visitLinebreak(final Linebreak node) {
      return singleton(_document.createElement("sbr"));
    }

    @Override
    public List<Node> renderLink(final String target, final String title, final Link node) {
      Element out = _document.createElement("link");
      out.setAttribute("xl:href", target);
      out.appendChild(_document.createTextNode(title));
      return singleton(out);
    }

    @Override
    public List<Node> visitListItem(final ListItem node) {
      return singleton(wraps("listitem", node));
    }

    @Override
    public List<Node> visitMacroNode(final MacroNode node) {
      return singleton(wraps(node.isBlock() ? "pre" : "code", node));
    }

    @Override
    public List<Node> visitOrderedList(final OrderedList node) {
      return singleton(wraps("orderedlist", node));
    }

    @Override
    public List<Node> visitParagraph(final Paragraph node) {
      return singleton(wraps("para", node));
    }

    @Override
    public List<Node> visitStrikethrough(final Strikethrough node) {
      Element strong = _document.createElement("emphasis");
      strong.setAttribute("role", "strike");
      return build(strong, visitASTNode(node));
    }

    @Override
    public List<Node> visitTable(final Table node) {
      return singleton(wraps("table", node));
    }

    @Override
    public List<Node> visitTableCell(final TableCell node) {
      Element out = (Element) wraps("td", node);

      if (isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        try {
          out.setAttribute("valign", unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0));
        }
        catch (Exception e) {
          System.err.println("Error when handling directive " + TABLE_ALIGNMENT_DIRECTIVE);
        }
      }

      return singleton(out);
    }

    @Override
    public List<Node> visitTableHeaderCell(final TableHeaderCell node) {
      Element out = (Element) wraps("th", node);

      if (isEnabled(TABLE_ALIGNMENT_DIRECTIVE)) {
        try {
          out.setAttribute("valign", unsafeGetArgs(TABLE_ALIGNMENT_DIRECTIVE).get(0));
        }
        catch (Exception e) {
          System.err.println("Error when handling directive " + TABLE_ALIGNMENT_DIRECTIVE);
        }
      }

      return singleton(out);
    }

    @Override
    public List<Node> visitTableRow(final TableRow node) {
      return singleton(wraps("tr", node));
    }

    @Override
    public List<Node> visitTextNode(final TextNode node) {
      String text = node.getText();
      return singleton(node.isEscaped() ? _document.createTextNode(text) : _document.createCDATASection(text));
    }

    @Override
    public List<Node> visitUnorderedList(final UnorderedList node) {
      return singleton(wraps("itemizedlist", node));
    }
  }
}
