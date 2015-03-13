package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;
import net.hillsdon.reviki.wiki.renderer.creole.ast.ASTRenderer.Languages;
import net.hillsdon.reviki.wiki.renderer.creole.Creole.*;

/**
 * Visitor which walks the parse tree to build a more programmer-friendly AST.
 * This also performs some non-standard rearrangement in order to replicate
 * functionality from the old renderer.
 *
 * @author msw
 */
public class Visitor extends CreoleASTBuilder {
  public Visitor(final SimplePageStore store, final PageInfo page, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super(store, page, linkHandler, imageHandler);
  }

  public Visitor(final PageInfo page, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super(page, linkHandler, imageHandler);
  }

  /**
   * Render the root node, creole. creole contains zero or more block elements
   * separated by linebreaks and paragraph breaks.
   *
   * The rendering behaviour is to render each block individually, and then
   * display them sequentially in order.
   */
  @Override
  public ASTNode visitCreole(final CreoleContext ctx) {
    List<ASTNode> blocks = new ArrayList<ASTNode>();

    for (BlockContext btx : ctx.block()) {
      ASTNode ren = visit(btx);

      // If we have a paragraph, rip off initial and trailing inline code and
      // macros. Otherwise, just add the block to the list as-is.
      if (ren instanceof Paragraph) {
        List<ASTNode> expandedInit = expandParagraph((Paragraph) ren, false);
        List<ASTNode> expandedTail = new ArrayList<ASTNode>();

        // Handle trailing things by extracting the chunks of the final
        // paragraph, reversing, extracting the prefix again, and then reversing
        // the result.
        if (expandedInit.size() > 0 && expandedInit.get(expandedInit.size() - 1) instanceof Paragraph) {
          Paragraph paragraph = (Paragraph) expandedInit.get(expandedInit.size() - 1);
          expandedInit.remove(paragraph);

          List<ASTNode> chunks = new ArrayList<ASTNode>(paragraph.getChildren().get(0).getChildren());
          Collections.reverse(chunks);

          expandedTail = expandParagraph(new Paragraph(new Inline(chunks)), true);
          Collections.reverse(expandedTail);
        }

        blocks.addAll(expandedInit);
        blocks.addAll(expandedTail);
      }
      else {
        blocks.add(ren);
      }
    }

    // Remove paragraphs just consisting of whitespace
    List<ASTNode> blocksNonEmpty = new ArrayList<ASTNode>();

    for (ASTNode block : blocks) {
      if (block != null && !(block instanceof Paragraph && block.toSmallString().trim().equals("Paragraph"))) {
        blocksNonEmpty.add(block);
      }
    }

    return new Page(blocksNonEmpty);
  }

  /**
   * Render a heading node. This consists of a prefix, the length of which will
   * tell us the heading level, and some content.
   */
  @Override
  public ASTNode visitHeading(final HeadingContext ctx) {
    if (ctx.inline() == null) {
      return new Plaintext(ctx.HSt().getText());
    }
    else {
      return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
    }
  }

  /**
   * Render a paragraph node. This consists of a single inline element.
   */
  @Override
  public ASTNode visitParagraph(final ParagraphContext ctx) {
    return new Paragraph(visit(ctx.inline()));
  }

  /**
   * Render an inline node. This consists of a list of chunks of smaller markup
   * units, which are displayed in order.
   */
  @Override
  public ASTNode visitInline(final InlineContext ctx) {
    return trimInline(visitInlineNoTrim(ctx));
  }

  /**
   * Render an any node. This consists of some plaintext, which is escaped and
   * displayed with no further processing.
   */
  @Override
  public ASTNode visitAny(final AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  /**
   * Render a WikiWords link.
   */
  @Override
  public ASTNode visitWikiwlink(final WikiwlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page(), linkHandler());
  }

  /**
   * Render an attachment link. If the attachment doesn't exist, it is rendered
   * in plain text.
   */
  @Override
  public ASTNode visitAttachment(final AttachmentContext ctx) {
    if (hasAttachment(ctx.getText())) {
      return new Link(ctx.getText(), ctx.getText(), page(), linkHandler());
    }
    else {
      return new Plaintext(ctx.getText());
    }
  }

  /**
   * Render a raw URL link.
   */
  @Override
  public ASTNode visitRawlink(final RawlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page(), linkHandler());
  }

  /**
   * Render an anchor.
   */
  @Override
  public ASTNode visitAnchor(AnchorContext ctx) {
    return new Anchor(ctx.InAnchor().getText());
  }

  /**
   * Render bold nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitBold(final BoldContext ctx) {
    return renderInlineMarkup(Bold.class, "**", "BEnd", ctx.BEnd(), ctx.inline());
  }

  /**
   * Render italic nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitItalic(final ItalicContext ctx) {
    return renderInlineMarkup(Italic.class, "//", "IEnd", ctx.IEnd(), ctx.inline());
  }

  /**
   * Render strikethrough nodes, with error recovery by {@link #renderInline}.
   */
  @Override
  public ASTNode visitSthrough(final SthroughContext ctx) {
    return renderInlineMarkup(Strikethrough.class, "--", "SEnd", ctx.SEnd(), ctx.inline());
  }

  /**
   * Render a link node with no title.
   */
  @Override
  public ASTNode visitLink(final LinkContext ctx) {
    return new Link(ctx.InLink().getText(), ctx.InLink().getText(), page(), linkHandler());
  }

  /**
   * Render JIRA style links with no title.
   */
  @Override
  public ASTNode visitJiralink(final JiralinkContext ctx) {
    String target = (ctx.JIRAInLink() == null) ? "" : ctx.JIRAInLink().getText();
    return new Link(target, target, page(), linkHandler());
  }

  /**
   * Render a link node with a title.
   */
  @Override
  public ASTNode visitTitlelink(final TitlelinkContext ctx) {
    String target = (ctx.InLink() == null) ? "" : ctx.InLink().getText();
    String title = (ctx.InLinkEnd() == null) ? target : ctx.InLinkEnd().getText();
    return new Link(target, title, page(), linkHandler());
  }

  /**
   * Render JIRA style link nodes with a title.
   */
  @Override
  public ASTNode visitJiratitlelink(final JiratitlelinkContext ctx) {
    String target = (ctx.JIRAInLinkEnd() == null) ? "" : ctx.JIRAInLinkEnd().getText();
    String title = (ctx.JIRAInLink() == null) ? target : ctx.JIRAInLink().getText();
    return new Link(target, title, page(), linkHandler());
  }

  /**
   * Render an image node.
   */
  @Override
  public ASTNode visitImglink(final ImglinkContext ctx) {
    String target = (ctx.InLink() == null) ? "" : ctx.InLink().getText();
    String title = (ctx.InLinkEnd() == null) ? target : ctx.InLinkEnd().getText();
    return new Image(target, title, page(), imageHandler());
  }

  /**
   * Render an image without a title.
   */
  @Override
  public ASTNode visitSimpleimg(final SimpleimgContext ctx) {
    return new Image(ctx.InLink().getText(), ctx.InLink().getText(), page(), imageHandler());
  }

  /**
   * Render an inline nowiki node.
   */
  @Override
  public ASTNode visitPreformat(final PreformatContext ctx) {
    return new InlineCode(ctx.NoWikiInlineAny().getText());
  }

  /**
   * Render a syntax-highlighted code block.
   */
  @Override
  public ASTNode visitInlinecodetag(final InlinecodetagContext ctx) {
    return new InlineCode(ctx.CodeTagInlineAny().getText(), findLanguageType(ctx.CodeTagStart()));
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitInlinehtml(final InlinehtmlContext ctx) {
    return new Raw(ctx.HtmlInlineAny().getText());
  }

  /**
   * Render a literal linebreak node.
   */
  @Override
  public ASTNode visitLinebreak(final LinebreakContext ctx) {
    return new Linebreak();
  }

  /**
   * Render a horizontal rule node.
   */
  @Override
  public ASTNode visitHrule(final HruleContext ctx) {
    return new HorizontalRule();
  }

  /**
   * Render an ordered list. List rendering is a bit of a mess, with lots of
   * almost code duplication due to the way I've handled nesting.
   *
   * TODO: Figure out how to nest lists arbitrarily deep.
   */
  @Override
  public ASTNode visitOlist(final OlistContext ctx) {
    List<ASTNode> children = new ArrayList<ASTNode>();

    for (ParserRuleContext rtx : ctx.olist1()) {
      children.add(visit(rtx));
    }

    return new OrderedList(children);
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist1(final Olist1Context ctx) {
    return renderListItem(ctx.list2(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist2(final Olist2Context ctx) {
    return renderListItem(ctx.list3(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist3(final Olist3Context ctx) {
    return renderListItem(ctx.list4(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist4(final Olist4Context ctx) {
    return renderListItem(ctx.list5(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist5(final Olist5Context ctx) {
    return renderListItem(ctx.list6(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist6(final Olist6Context ctx) {
    return renderListItem(ctx.list7(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist7(final Olist7Context ctx) {
    return renderListItem(ctx.list8(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist8(final Olist8Context ctx) {
    return renderListItem(ctx.list9(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist9(final Olist9Context ctx) {
    return renderListItem(ctx.list10(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist10(final Olist10Context ctx) {
    return renderListItem(Collections.<ParserRuleContext> emptyList(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist(final UlistContext ctx) {
    List<ASTNode> children = new ArrayList<ASTNode>();

    for (ParserRuleContext rtx : ctx.ulist1()) {
      children.add(visit(rtx));
    }

    return new UnorderedList(children);
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist1(final Ulist1Context ctx) {
    return renderListItem(ctx.list2(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist2(final Ulist2Context ctx) {
    return renderListItem(ctx.list3(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist3(final Ulist3Context ctx) {
    return renderListItem(ctx.list4(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist4(final Ulist4Context ctx) {
    return renderListItem(ctx.list5(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist5(final Ulist5Context ctx) {
    return renderListItem(ctx.list6(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist6(final Ulist6Context ctx) {
    return renderListItem(ctx.list7(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist7(final Ulist7Context ctx) {
    return renderListItem(ctx.list8(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist8(final Ulist8Context ctx) {
    return renderListItem(ctx.list9(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist9(final Ulist9Context ctx) {
    return renderListItem(ctx.list10(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist10(final Ulist10Context ctx) {
    return renderListItem(Collections.<ParserRuleContext> emptyList(), ctx.inList());
  }

  /**
   * Render a NoWiki block node.
   */
  @Override
  public ASTNode visitNowiki(final NowikiContext ctx) {
    return new Code(ctx.NoWikiAny().getText());
  }

  /**
   * Like {@link #visitInlinecode}, but for blocks.
   */
  @Override
  public ASTNode visitCodetag(final CodetagContext ctx) {
    return new Code(ctx.CodeTagAny().getText(), findLanguageType(ctx.CodeTagStart()));
  }

  /**
   * Render a codeblock.
   */
  @Override
  public ASTNode visitCodeblock(final CodeblockContext ctx) {
    return new Code(ctx.CodeAny().getText(), findLanguageType(ctx.CodeStart()));
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitHtml(final HtmlContext ctx) {
    return new Raw(ctx.HtmlAny().getText());
  }

  /**
   * Render a table node. This consists of rendering all rows in sequence.
   */
  @Override
  public ASTNode visitTable(final TableContext ctx) {
    List<ASTNode> rows = new ArrayList<ASTNode>();

    for (TrowContext rtx : ctx.trow()) {
      rows.add(visit(rtx));
    }

    return new Table(rows);
  }

  /**
   * Render a table row. A table row consists of a number of cells, and a
   * possible trailing separator.
   */
  @Override
  public ASTNode visitTrow(final TrowContext ctx) {
    List<ASTNode> cells = new ArrayList<ASTNode>();

    for (TcellContext rtx : ctx.tcell()) {
      cells.add(visit(rtx));
    }

    return new TableRow(cells);
  }

  /**
   * Render a table heading cell.
   */
  @Override
  public ASTNode visitTh(final ThContext ctx) {
    ASTNode body;
    if (ctx.inline() == null) {
      body = new Inline(Arrays.asList(new ASTNode[] { new Plaintext("") }));
    }
    else {
      body = visit(ctx.inline());
    }

    return new TableHeaderCell(body);
  }

  /**
   * Render a table cell.
   */
  @Override
  public ASTNode visitTd(final TdContext ctx) {
    List<ASTNode> inner = new ArrayList<ASTNode>();
    for (InTableContext itx : ctx.inTable()) {
      if (itx == null || visit(itx) == null)
        continue;

      inner.add(visit(itx));
    }

    if (inner.isEmpty()) {
      inner.add(new Plaintext(""));
    }

    return new TableCell(inner);
  }

  /**
   * Render a macro.
   */
  @Override
  public ASTNode visitMacro(final MacroContext ctx) {
    // If there are no arguments, it's not a macro
    if (ctx.MacroEndNoArgs() != null) {
      return new Plaintext("<<" + ctx.MacroName().getText() + ">>");
    }
    else {
      return new MacroNode(ctx.MacroName().getText(), cutOffEndTag(ctx.MacroEnd(), ">>"), this);
    }
  }

  /**
   * Render a terseblockquote.
   */
  @Override
  public ASTNode visitTerseblockquote(TerseblockquoteContext ctx) {
    return new Blockquote(visit(ctx.creole()));
  }

  /**
   * Render a blockquote.
   */
  @Override
  public ASTNode visitBlockquote(BlockquoteContext ctx) {
    return new Blockquote(visit(ctx.creole()));
  }

  /**
   * Enable a directive.
   */
  @Override
  public ASTNode visitEnable(EnableContext ctx) {
    if (ctx.MacroEndNoArgs() == null) {
      return new DirectiveNode(ctx.MacroName().getText(), true, cutOffEndTag(ctx.MacroEnd(), ">>"));
    }
    else {
      return new DirectiveNode(ctx.MacroName().getText(), true);
    }
  }

  /**
   * Disable a directive.
   */
  @Override
  public ASTNode visitDisable(final DisableContext ctx) {
    if (ctx.MacroEndNoArgs() == null) {
      return new DirectiveNode(ctx.MacroName().getText(), false, cutOffEndTag(ctx.MacroEnd(), ">>"));
    }
    else {
      return new DirectiveNode(ctx.MacroName().getText(), false);
    }
  }
}
