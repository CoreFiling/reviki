package net.hillsdon.reviki.converter;

import java.util.Arrays;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.impl.PageReferenceImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Blockquote;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Bold;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Code;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Heading;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Image;
import net.hillsdon.reviki.wiki.renderer.creole.ast.InlineCode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.InlineNowiki;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Italic;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Linebreak;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Link;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ListItem;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Nowiki;
import net.hillsdon.reviki.wiki.renderer.creole.ast.OrderedList;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Paragraph;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Plaintext;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Strikethrough;
import net.hillsdon.reviki.wiki.renderer.creole.ast.Table;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableCell;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableHeaderCell;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TableRow;
import net.hillsdon.reviki.wiki.renderer.creole.ast.TextNode;
import net.hillsdon.reviki.wiki.renderer.creole.ast.UnorderedList;

class MarkdownCreoleRenderer extends ASTRenderer<String> {

  private enum ListType {
    ORDERED,
    UNORDERED
  }

  private final Stack<ListType> _listTypes = new Stack<MarkdownCreoleRenderer.ListType>();
  private boolean _isFirstRow;
  private final LinkResolutionContext _linkResolutionContext;

  public MarkdownCreoleRenderer(final LinkResolutionContext linkResolutionContext) {
    super("", URLOutputFilter.NULL);
    _linkResolutionContext = linkResolutionContext;
  }

  @Override
  protected String combine(final String x1, final String x2) {
    return x1 + x2;
  }

  @Override
  public String visitHeading(final Heading node) {
    return ensureNewLinesAfter(StringUtils.repeat("#", node.getLevel()) + " " + visitASTNode(node), 2);
  }

  @Override
  public String visitLinebreak(final Linebreak node) {
    return "<br>";
  }

  @Override
  public String visitTextNode(final TextNode node) {
    return node.isEscaped() ? Escape.html(node.getText()) : node.getText();
  }

  @Override
  public String visitParagraph(final Paragraph node) {
    return ensureNewLinesAfter(visitASTNode(node), 2);
  }

  @Override
  public String visitListItem(final ListItem node) {
    return ensureNewLineAfter(padLines(visitASTNode(node), listItemPrefix(), false));
  }

  private String listItemPrefix() {
    if (_listTypes.isEmpty()) {
      throw new IllegalStateException("Unexpected list item outside of a list");
    }
    switch (_listTypes.peek()) {
      case ORDERED:
        return "1.  ";
      case UNORDERED:
        return "*   ";
      default:
        // Bad enum value
        throw new IllegalStateException("Invalid list type");
    }
  }

  @Override
  public String visitOrderedList(final OrderedList node) {
    return visitList(node, ListType.ORDERED);
  }

  @Override
  public String visitUnorderedList(final UnorderedList node) {
    return visitList(node, ListType.UNORDERED);
  }

  private String visitList(final ASTNode node, final ListType type) {
    boolean nested = !_listTypes.isEmpty();
    _listTypes.push(type);
    String str = (nested ? "\n" : "") + visitASTNode(node) + (nested ? "" : "\n");
    _listTypes.pop();
    return str;
  }

  @Override
  public String visitLink(final Link node) {
    try {
      LinkResolutionContext context = _linkResolutionContext.derive(new PageReferenceImpl("foo"));
      return "[" + Optional.fromNullable(node.getTitle()).or("") + "](" + node.getParts().getURL(context) + ")";
    }
    catch (Exception e) {
      // Fall through to return text.
      // This is horrid but it's what real Reviki does to handle non-existent internal pages.
    }
    return node.getTitle() == null ? node.getTarget() : node.getTitle();
  }

  @Override
  public String visitImage(final Image node) {
    return "![" + node.getTitle() + "](" + node.getTarget() + ")";
  }

  @Override
  public String visitBold(final Bold node) {
    return "**" + visitASTNode(node) + "**";
  }

  @Override
  public String visitItalic(final Italic node) {
    return "*" + visitASTNode(node) + "*";
  }

  @Override
  public String visitStrikethrough(final Strikethrough node) {
    return "~~" + visitASTNode(node) + "~~";
  }

  @Override
  public String visitCode(final Code node) {
    return "```" + node.getLanguage().or("") + "\n" + ensureNewLineAfter(node.getText()) + "```\n";
  }

  @Override
  public String visitInlineCode(final InlineCode node) {
    // How would you even do language?
    return "`" + node.getText() + "`";
  }

  @Override
  public String visitPlaintext(final Plaintext node) {
    return node.isEscaped() ? Escape.html(node.getText()) : node.getText();
  }

  @Override
  public String visitBlockquote(final Blockquote node) {
    return ensureNewLinesAfter(padLines(visitASTNode(node), "> ", true), 2);
  }

  @Override
  public String visitNowiki(final Nowiki node) {
    return "```\n" + ensureNewLineAfter(node.getText()) + "```\n";
  }

  @Override
  public String visitInlineNowiki(final InlineNowiki node) {
    return "`" + node.getText() + "`";
  }

  @Override
  public String visitTable(final Table node) {
    if (node.getChildren().isEmpty() || node.getChildren().get(0).getChildren().size() == 0) {
      // I don't think this is possible, but just in case
      return "";
    }
    StringBuilder table = new StringBuilder();
    int columns = node.getChildren().get(0).getChildren().size();

    _isFirstRow = true;
    table.append(visit(node.getChildren().get(0)));
    _isFirstRow = false;

    table.append("|" + Strings.repeat("---|", columns - 1) + "---|\n");

    for (ASTNode childNode : node.getChildren().subList(1, node.getChildren().size())) {
      table.append(visit(childNode));
    }
    table.append("\n");
    return table.toString();
  }

  @Override
  public String visitTableRow(final TableRow node) {
    StringBuilder row = new StringBuilder();
    for (ASTNode cellNode : node.getChildren()) {
      row.append("| " + visit(cellNode) + " ");
    }
    row.append("|\n");
    return row.toString();
  }

  @Override
  public String visitTableHeaderCell(final TableHeaderCell node) {
    if (_isFirstRow) {
      return visitASTNode(node);
    }
    // Hack to sort of support row headers.
    return "**" + visitASTNode(node) + "**";
  }

  @Override
  public String visitTableCell(final TableCell node) {
    return visitASTNode(node);
  }

  private String padLines(final String text, final String prefix, final boolean allLines) {
    return Joiner.on("\n").join(Iterables.transform(Arrays.asList(text.split("\n")), new Function<String, String>() {
      private boolean _firstLine = true;
      @Override
      public String apply(final String line) {
        if (allLines || _firstLine) {
          _firstLine = false;
          return prefix + line;
        }
        return StringUtils.repeat(" ", prefix.length()) + line;
      }
    }));
  }

  private String ensureNewLineAfter(final String text) {
    return ensureNewLinesAfter(text, 1);
  }

  private String ensureNewLinesAfter(final String text, final int count) {
    return StringUtils.stripEnd(text, "\n") + StringUtils.repeat("\n", count);
  }
}