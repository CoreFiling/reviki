package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.parser.Creole.*;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.*;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

/**
 * Visitor which walks the parse tree to build a more programmer-friendly AST.
 * This also performs some non-standard rearrangement in order to replicate
 * functionality from the old renderer.
 *
 * @author msw
 */
public class Visitor extends CreoleASTBuilder {
  public Visitor(PageInfo page, URLOutputFilter urlOutputFilter, LinkPartsHandler handler) {
    super(page, urlOutputFilter, handler);
  }

  /**
   * Render the root node, creole. creole contains zero or more block elements
   * separated by linebreaks and paragraph breaks.
   *
   * The rendering behaviour is to render each block individually, and then
   * display them sequentially in order.
   */
  @Override
  public ResultNode visitCreole(CreoleContext ctx) {
    List<ResultNode> blocks = new ArrayList<ResultNode>();

    for (BlockContext btx : ctx.block()) {
      ResultNode ren = visit(btx);
      if (ren != null) {
        blocks.add(ren);
      }
    }

    return new Page(blocks);
  }

  /**
   * Render a heading node. This consists of a prefix, the length of which will
   * tell us the heading level, and some content.
   */
  @Override
  public ResultNode visitHeading(HeadingContext ctx) {
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
  }

  /**
   * Render a paragraph node. This consists of a single inline element. Leading
   * and trailing newlines are stripped, and if the paragraph consists solely of
   * an inline nowiki line, it is instead rendered as a nowiki block, to
   * replicate old behaviour.
   */
  @Override
  public ResultNode visitParagraph(ParagraphContext ctx) {
    ResultNode body = visit(ctx.inline());

    ResultNode inner = body;

    // Drop leading and trailing newlines (TODO: Figure out how to do this in
    // the grammar, along with all the other stuff)
    if (body instanceof Inline) {
      int children = body.getChildren().size();
      if (children > 0 && body.getChildren().get(0).toXHTML().equals("\n")) {
        body = new Inline(body.getChildren().subList(1, children));
        children--;
      }

      if (children > 0 && body.getChildren().get(children - 1).toXHTML().equals("\n")) {
        body = new Inline(body.getChildren().subList(0, children - 1));
      }
    }

    // If a paragraph contains nothing but an inline nowiki element, render that
    // as a block nowiki element. Not quite to spec, but replicates old
    // behaviour.
    if (body instanceof Inline && body.getChildren().size() == 1) {
      inner = body.getChildren().get(0);
    }

    if (inner instanceof InlineCode) {
      return ((InlineCode) inner).toBlock();
    }

    return new Paragraph(body);
  }

  /**
   * Render an inline node. This consists of a list of chunks of smaller markup
   * units, which are displayed in order.
   */
  @Override
  public ResultNode visitInline(InlineContext ctx) {
    List<ResultNode> chunks = new ArrayList<ResultNode>();

    for (InlinestepContext itx : ctx.inlinestep()) {
      chunks.add(visit(itx));
    }

    return new Inline(chunks);
  }

  /**
   * Render an any node. This consists of some plaintext, which is escaped and
   * displayed with no further processing.
   */
  @Override
  public ResultNode visitAny(AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  /**
   * Render a WikiWords link.
   */
  @Override
  public ResultNode visitWikiwlink(WikiwlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, handler);
  }

  /**
   * Render a raw URL link.
   */
  @Override
  public ResultNode visitRawlink(RawlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, handler);
  }

  /**
   * Render bold nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ResultNode visitBold(BoldContext ctx) {
    return renderInlineMarkup(Bold.class, "**", "BEnd", ctx.BEnd(), ctx.inline());
  }

  /**
   * Render italic nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ResultNode visitItalic(ItalicContext ctx) {
    return renderInlineMarkup(Italic.class, "//", "IEnd", ctx.IEnd(), ctx.inline());
  }

  /**
   * Render strikethrough nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ResultNode visitSthrough(SthroughContext ctx) {
    return renderInlineMarkup(Strikethrough.class, "--", "SEnd", ctx.SEnd(), ctx.inline());
  }

  /**
   * Render a link node with no title.
   */
  @Override
  public ResultNode visitLink(LinkContext ctx) {
    return new Link(ctx.InLink().getText(), ctx.InLink().getText(), page, urlOutputFilter, handler);
  }

  /**
   * Render a link node with a title.
   */
  @Override
  public ResultNode visitTitlelink(TitlelinkContext ctx) {
    return new Link(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, handler);
  }

  /**
   * Render an image node.
   */
  @Override
  public ResultNode visitImglink(ImglinkContext ctx) {
    return new Image(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, handler);
  }

  /**
   * Render an inline nowiki node. Due to how the lexer works, the contents
   * include the ending symbol, which must be chopped off.
   *
   * TODO: Improve the tokensiation of this.
   */
  @Override
  public ResultNode visitPreformat(PreformatContext ctx) {
    return renderInlineCode(ctx.EndNoWikiInline(), "}}}");
  }

  /**
   * Render a syntax-highlighted CPP block. This has the same tokenisation
   * problem as mentioned in {@link #visitPreformat}.
   */
  @Override
  public ResultNode visitInlinecpp(InlinecppContext ctx) {
    return renderInlineCode(ctx.EndCppInline(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ResultNode visitInlinehtml(InlinehtmlContext ctx) {
    String code = ctx.EndHtmlInline().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ResultNode visitInlinejava(InlinejavaContext ctx) {
    return renderInlineCode(ctx.EndJavaInline(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ResultNode visitInlinexhtml(InlinexhtmlContext ctx) {
    return renderInlineCode(ctx.EndXhtmlInline(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ResultNode visitInlinexml(InlinexmlContext ctx) {
    return renderInlineCode(ctx.EndXmlInline(), "[</xml>]", XhtmlRendererFactory.XML);
  }

  /**
   * Render a literal linebreak node
   */
  @Override
  public ResultNode visitLinebreak(LinebreakContext ctx) {
    return new Linebreak();
  }

  /**
   * Render a horizontal rule node.
   */
  @Override
  public ResultNode visitHrule(HruleContext ctx) {
    return new HorizontalRule();
  }

  /**
   * Render an ordered list. List rendering is a bit of a mess at the moment,
   * but it basically works like this:
   *
   * - Lists have a root node, which contains level 1 elements.
   *
   * - There are levels 1 through 5.
   *
   * - Each level (other than 5) may have child list elements.
   *
   * - Each level (other than root) may also have some content, which can be a
   * new list (ordered or unordered), or inline text.
   *
   * The root list node is rendered by displaying all level 1 entities in
   * sequence, where level n entities are rendered by displaying their content
   * and then any children they have.
   *
   * TODO: Figure out how to have arbitrarily-nested lists.
   */
  @Override
  public ResultNode visitOlist(OlistContext ctx) {
    return renderList(OrderedList.class, ctx.olist1(), null, null, null);
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitOlist1(Olist1Context ctx) {
    return renderList(OrderedList.class, ctx.olist2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitOlist2(Olist2Context ctx) {
    return renderList(OrderedList.class, ctx.olist3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitOlist3(Olist3Context ctx) {
    return renderList(OrderedList.class, ctx.olist4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitOlist4(Olist4Context ctx) {
    return renderList(OrderedList.class, ctx.olist5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitOlist5(Olist5Context ctx) {
    return renderList(OrderedList.class, new ArrayList<ParserRuleContext>(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist(UlistContext ctx) {
    return renderList(UnorderedList.class, ctx.ulist1(), null, null, null);
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist1(Ulist1Context ctx) {
    return renderList(UnorderedList.class, ctx.ulist2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist2(Ulist2Context ctx) {
    return renderList(UnorderedList.class, ctx.ulist3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist3(Ulist3Context ctx) {
    return renderList(UnorderedList.class, ctx.ulist4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist4(Ulist4Context ctx) {
    return renderList(UnorderedList.class, ctx.ulist5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ResultNode visitUlist5(Ulist5Context ctx) {
    return renderList(UnorderedList.class, new ArrayList<ParserRuleContext>(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /**
   * Render a NoWiki block node. This has the same tokenisation problem as
   * mentioned in {@link #visitPreformat}.
   */
  @Override
  public ResultNode visitNowiki(NowikiContext ctx) {
    return renderBlockCode(ctx.EndNoWikiBlock(), "}}}");
  }

  /**
   * Like {@link #visitInlinecpp}, but for blocks.
   */
  @Override
  public ResultNode visitCpp(CppContext ctx) {
    return renderBlockCode(ctx.EndCppBlock(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ResultNode visitHtml(HtmlContext ctx) {
    String code = ctx.EndHtmlBlock().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ResultNode visitJava(JavaContext ctx) {
    return renderBlockCode(ctx.EndJavaBlock(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ResultNode visitXhtml(XhtmlContext ctx) {
    return renderBlockCode(ctx.EndXhtmlBlock(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ResultNode visitXml(XmlContext ctx) {
    return renderBlockCode(ctx.EndXmlBlock(), "[</xml>]", XhtmlRendererFactory.XML);
  }

  /**
   * Render a table node. This consists of rendering all rows in sequence.
   */
  @Override
  public ResultNode visitTable(TableContext ctx) {
    List<ResultNode> rows = new ArrayList<ResultNode>();

    for (TrowContext rtx : ctx.trow()) {
      rows.add(visit(rtx));
    }

    return new Table(rows);
  }

  /**
   * Render a table row. A table row consists of a number of cells, and a
   * possible trailing separator. At the moment, the trailing separator is
   * handled explicitly here, rather than nicely in the grammar.
   *
   * TODO: Handle the trailing separator in the grammar, and remove the check
   * here.
   */
  @Override
  public ResultNode visitTrow(TrowContext ctx) {
    List<ResultNode> cells = new ArrayList<ResultNode>();

    for (TcellContext rtx : ctx.tcell()) {
      cells.add(visit(rtx));
    }

    // If the last cell is empty, it's a trailing separator - not actually a new
    // cell.
    if (cells.size() != 0) {
      ResultNode last = cells.get(cells.size() - 1);
      if (last instanceof TableCell && last.getChildren().get(0).toXHTML().equals("")) {
        cells.remove(last);
      }
    }

    return new TableRow(cells);
  }

  /**
   * Render a table heading cell.
   */
  @Override
  public ResultNode visitTh(ThContext ctx) {
    return new TableHeaderCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  /**
   * Render a table cell.
   */
  @Override
  public ResultNode visitTd(TdContext ctx) {
    return new TableCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }
}
