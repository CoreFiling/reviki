package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.creole.Creole.*;

/**
 * Visitor which walks the parse tree to build a more programmer-friendly AST.
 * This also performs some non-standard rearrangement in order to replicate
 * functionality from the old renderer.
 *
 * @author msw
 */
public class Visitor extends CreoleASTBuilder {
  public Visitor(PageInfo page, URLOutputFilter urlOutputFilter, LinkPartsHandler linkHandler, LinkPartsHandler imageHandler) {
    super(page, urlOutputFilter, linkHandler, imageHandler);
  }

  /**
   * Render the root node, creole. creole contains zero or more block elements
   * separated by linebreaks and paragraph breaks.
   *
   * The rendering behaviour is to render each block individually, and then
   * display them sequentially in order.
   */
  @Override
  public ASTNode visitCreole(CreoleContext ctx) {
    List<ASTNode> blocks = new ArrayList<ASTNode>();

    for (BlockContext btx : ctx.block()) {
      ASTNode ren = visit(btx);
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
  public ASTNode visitHeading(HeadingContext ctx) {
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
  }

  /**
   * Render a paragraph node. This consists of a single inline element. Leading
   * and trailing newlines are stripped, and if the paragraph consists solely of
   * an inline nowiki line, it is instead rendered as a nowiki block, to
   * replicate old behaviour.
   */
  @Override
  public ASTNode visitParagraph(ParagraphContext ctx) {
    ASTNode body = visit(ctx.inline());

    ASTNode inner = body;

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

    // If a paragraph contains only a macro node, remove the enclosing
    // paragraph.
    if (inner instanceof MacroNode) {
      return inner;
    }

    return new Paragraph(body);
  }

  /**
   * Render an inline node. This consists of a list of chunks of smaller markup
   * units, which are displayed in order.
   */
  @Override
  public ASTNode visitInline(InlineContext ctx) {
    List<ASTNode> chunks = new ArrayList<ASTNode>();

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
  public ASTNode visitAny(AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  /**
   * Render a WikiWords link.
   */
  @Override
  public ASTNode visitWikiwlink(WikiwlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, linkHandler);
  }

  /**
   * Render a raw URL link.
   */
  @Override
  public ASTNode visitRawlink(RawlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, linkHandler);
  }

  /**
   * Render bold nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitBold(BoldContext ctx) {
    return renderInlineMarkup(Bold.class, "**", "BEnd", ctx.BEnd(), ctx.inline());
  }

  /**
   * Render italic nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitItalic(ItalicContext ctx) {
    return renderInlineMarkup(Italic.class, "//", "IEnd", ctx.IEnd(), ctx.inline());
  }

  /**
   * Render strikethrough nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitSthrough(SthroughContext ctx) {
    return renderInlineMarkup(Strikethrough.class, "--", "SEnd", ctx.SEnd(), ctx.inline());
  }

  /**
   * Render a link node with no title.
   */
  @Override
  public ASTNode visitLink(LinkContext ctx) {
    return new Link(ctx.InLink().getText(), ctx.InLink().getText(), page, urlOutputFilter, linkHandler);
  }

  /**
   * Render a link node with a title.
   */
  @Override
  public ASTNode visitTitlelink(TitlelinkContext ctx) {
    return new Link(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, linkHandler);
  }

  /**
   * Render an image node.
   */
  @Override
  public ASTNode visitImglink(ImglinkContext ctx) {
    return new Image(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, imageHandler);
  }

  /**
   * Render an inline nowiki node. Due to how the lexer works, the contents
   * include the ending symbol, which must be chopped off.
   *
   * TODO: Improve the tokensiation of this.
   */
  @Override
  public ASTNode visitPreformat(PreformatContext ctx) {
    return renderInlineCode(ctx.EndNoWikiInline(), "}}}");
  }

  /**
   * Render a syntax-highlighted CPP block. This has the same tokenisation
   * problem as mentioned in {@link #visitPreformat}.
   */
  @Override
  public ASTNode visitInlinecpp(InlinecppContext ctx) {
    return renderInlineCode(ctx.EndCppInline(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitInlinehtml(InlinehtmlContext ctx) {
    String code = ctx.EndHtmlInline().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ASTNode visitInlinejava(InlinejavaContext ctx) {
    return renderInlineCode(ctx.EndJavaInline(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ASTNode visitInlinexhtml(InlinexhtmlContext ctx) {
    return renderInlineCode(ctx.EndXhtmlInline(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode} */
  @Override
  public ASTNode visitInlinexml(InlinexmlContext ctx) {
    return renderInlineCode(ctx.EndXmlInline(), "[</xml>]", XhtmlRendererFactory.XML);
  }

  /**
   * Render a literal linebreak node
   */
  @Override
  public ASTNode visitLinebreak(LinebreakContext ctx) {
    return new Linebreak();
  }

  /**
   * Render a horizontal rule node.
   */
  @Override
  public ASTNode visitHrule(HruleContext ctx) {
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
  public ASTNode visitOlist(OlistContext ctx) {
    return renderList(ListType.Ordered, ctx.olist1());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist1(Olist1Context ctx) {
    return renderListItem(ListType.Ordered, ctx.olist2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist2(Olist2Context ctx) {
    return renderListItem(ListType.Ordered, ctx.olist3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist3(Olist3Context ctx) {
    return renderListItem(ListType.Ordered, ctx.olist4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist4(Olist4Context ctx) {
    return renderListItem(ListType.Ordered, ctx.olist5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist5(Olist5Context ctx) {
    return renderListItem(ListType.Ordered, new ArrayList<ParserRuleContext>(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist(UlistContext ctx) {
    return renderList(ListType.Unordered, ctx.ulist1());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist1(Ulist1Context ctx) {
    return renderListItem(ListType.Unordered, ctx.ulist2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist2(Ulist2Context ctx) {
    return renderListItem(ListType.Unordered, ctx.ulist3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist3(Ulist3Context ctx) {
    return renderListItem(ListType.Unordered, ctx.ulist4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist4(Ulist4Context ctx) {
    return renderListItem(ListType.Unordered, ctx.ulist5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist5(Ulist5Context ctx) {
    return renderListItem(ListType.Unordered, new ArrayList<ParserRuleContext>(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /**
   * Render a NoWiki block node. This has the same tokenisation problem as
   * mentioned in {@link #visitPreformat}.
   */
  @Override
  public ASTNode visitNowiki(NowikiContext ctx) {
    return renderBlockCode(ctx.EndNoWikiBlock(), "}}}");
  }

  /**
   * Like {@link #visitInlinecpp}, but for blocks.
   */
  @Override
  public ASTNode visitCpp(CppContext ctx) {
    return renderBlockCode(ctx.EndCppBlock(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitHtml(HtmlContext ctx) {
    String code = ctx.EndHtmlBlock().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ASTNode visitJava(JavaContext ctx) {
    return renderBlockCode(ctx.EndJavaBlock(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ASTNode visitXhtml(XhtmlContext ctx) {
    return renderBlockCode(ctx.EndXhtmlBlock(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode} */
  @Override
  public ASTNode visitXml(XmlContext ctx) {
    return renderBlockCode(ctx.EndXmlBlock(), "[</xml>]", XhtmlRendererFactory.XML);
  }

  /**
   * Render a table node. This consists of rendering all rows in sequence.
   */
  @Override
  public ASTNode visitTable(TableContext ctx) {
    List<ASTNode> rows = new ArrayList<ASTNode>();

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
  public ASTNode visitTrow(TrowContext ctx) {
    List<ASTNode> cells = new ArrayList<ASTNode>();

    for (TcellContext rtx : ctx.tcell()) {
      cells.add(visit(rtx));
    }

    // If the last cell is empty, it's a trailing separator - not actually a new
    // cell.
    if (cells.size() != 0) {
      ASTNode last = cells.get(cells.size() - 1);
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
  public ASTNode visitTh(ThContext ctx) {
    return new TableHeaderCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  /**
   * Render a table cell.
   */
  @Override
  public ASTNode visitTd(TdContext ctx) {
    return new TableCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  /**
   * Render a macro.
   */
  @Override
  public ASTNode visitMacro(MacroContext ctx) {
    return new MacroNode(ctx.MacroName().getText(), cutOffEndTag(ctx.MacroEnd(), ">>"), page, urlOutputFilter, linkHandler, imageHandler);
  }
}
