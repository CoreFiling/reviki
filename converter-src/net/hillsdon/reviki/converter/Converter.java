package net.hillsdon.reviki.converter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.hillsdon.fij.text.Escape;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.web.urls.UnknownWikiException;
import net.hillsdon.reviki.wiki.renderer.creole.Creole;
import net.hillsdon.reviki.wiki.renderer.creole.CreoleTokens;
import net.hillsdon.reviki.wiki.renderer.creole.ExternalLinkTarget;
import net.hillsdon.reviki.wiki.renderer.creole.LinkParts;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.LinkResolutionContext;
import net.hillsdon.reviki.wiki.renderer.creole.Visitor;
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

public class Converter {

  private enum ListType {
    NONE,
    ORDERED,
    UNORDERED
  }

  private static final Pattern JIRA_LINK = Pattern.compile("[A-Z]{2,}-[0-9]+");

  private static Optional<ParseTree> tryParse(final CommonTokenStream tokens, final Creole parser, final PredictionMode pmode) {
    parser.getInterpreter().setPredictionMode(pmode);
    try {
      return Optional.of((ParseTree) parser.creole());
    }
    catch (Exception e) {
      tokens.reset();
      parser.reset();

      return Optional.<ParseTree> absent();
    }
  }

	public static void main(final String[] args) throws IOException {
	  InputStream stream = System.in;
	  if (args.length == 1) {
	    if (!args[0].equals("-")) {
	      stream = new FileInputStream(args[0]);
	    }
	  }
	  else {
	    System.out.println("Usage: java -jar reviki-converter.jar <input_file | - >");
	    System.out.println();
	    System.out.println("  input_file  : reviki formatted file to convert");
	    System.out.println("                use '-' to read from stdin");
	    System.exit(1);
	  }

	  String reviki = IOUtils.readLines(stream).stream().collect(Collectors.joining("\n"));

	  CreoleTokens lexer = new CreoleTokens(null);
    lexer.setInputStream(new ANTLRInputStream(reviki));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Creole parser = new Creole(tokens);

    // First try parsing in SLL mode. This is really fast for pages with no
    // parse errors.
    Optional<ParseTree> tree = tryParse(tokens, parser, PredictionMode.SLL);

    if (!tree.isPresent()) {
      tree = tryParse(tokens, parser, PredictionMode.LL);
    }

    if (!tree.isPresent()) {
      System.err.println("Failed to parse reviki in stdin");
      System.exit(1);
    }

    LinkPartsHandler handler = new LinkPartsHandler() {

      @Override
      public boolean isAcronymNotLink(final LinkParts parts) {
        return true;
      }

      @Override
      public String handle(final PageReference page, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        return null;
      }

      @Override
      public String handle(final PageReference page, final String xhtmlContent, final LinkParts parts, final URLOutputFilter urlOutputFilter) throws URISyntaxException, UnknownWikiException {
        return null;
      }

      @Override
      public LinkResolutionContext getContext() {
        return null;
      }
    };

    Visitor visitor = new Visitor(new PageInfoImpl("foo"), handler, handler);

    System.out.println(new ASTRenderer<String>("", URLOutputFilter.NULL) {
      int _listLevel = -1;
      ListType _listType = ListType.NONE;
      private boolean _isFirstRow;

      @Override
      protected String combine(final String x1, final String x2) {
        return x1 + x2;
      }

      @Override
      public String visitHeading(final Heading node) {
        return StringUtils.repeat("#", node.getLevel()) + " " + visitASTNode(node) + "\n\n";
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
        return visitASTNode(node) + "\n\n";
      }

      @Override
      public String visitListItem(final ListItem node) {
        if (_listType == ListType.ORDERED) {
          return StringUtils.repeat("    ", _listLevel) + "1. " + visitASTNode(node) + "\n";
        }
        if (_listType == ListType.UNORDERED) {
          return StringUtils.repeat("    ", _listLevel) + "* " + visitASTNode(node) + "\n";
        }
        throw new IllegalStateException("Unexpected list item outside of a list");
      }

      @Override
      public String visitOrderedList(final OrderedList node) {
        _listType = ListType.ORDERED;
        _listLevel++;
        String str = visitASTNode(node) + "\n";
        _listLevel--;
        _listType = ListType.NONE;
        return str;
      }

      @Override
      public String visitUnorderedList(final UnorderedList node) {
        _listType = ListType.UNORDERED;
        _listLevel++;
        String str = visitASTNode(node) + "\n";
        _listLevel--;
        _listType = ListType.NONE;
        return str;
      }

      @Override
      public String visitLink(final Link node) {
        if (JIRA_LINK.matcher(node.getTarget()).matches()) {
          return "[" + node.getTitle() + "](https://jira.int.corefiling.com/browse/" + node.getTarget() + ")";
        }
        if (node.getParts().getTarget() instanceof ExternalLinkTarget) {
          return "[" + node.getTitle() + "](" + node.getTarget() + ")";
        }
        // Possibly wiki link of some sort so just print the text
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
        return "```" + node.getLanguage().or("") + "\n" + node.getText() + "\n```\n";
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
        return Joiner.on("\n").join(Iterables.transform(Arrays.asList(visitASTNode(node).split("\n")), new Function<String, String>() {
          @Override
          public String apply(final String line) {
            return "> " + line;
          }
        }));
      }

      @Override
      public String visitNowiki(final Nowiki node) {
        return "```\n" + node.getText() + "\n```\n";
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

        table.append("\n");
        table.append("|" + Strings.repeat("---|", columns - 1) + "---|\n");

        for (ASTNode childNode : node.getChildren().subList(1, node.getChildren().size())) {
          table.append(visit(childNode));
          table.append("\n");
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
        row.append("|");
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

    }.visit(visitor.visit(tree.get())));
  }

}
