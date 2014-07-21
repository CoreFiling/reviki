package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;

import net.hillsdon.reviki.vc.AttachmentHistory;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
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
  public Visitor(PageStore store, PageInfo page, URLOutputFilter urlOutputFilter, LinkPartsHandler linkHandler, LinkPartsHandler imageHandler) {
    super(store, page, urlOutputFilter, linkHandler, imageHandler);
  }

  /**
   * If a paragraph starts with a sequence of inline code or macros, separated
   * by newlines, render them as blocks and the remaining text (if any) as a
   * paragraph following this.
   *
   * @param paragraph The paragraph to expand out.
   * @param reversed Whether the paragraph is reversed or not. If so, the final
   *          trailing paragraph has its chunks reversed, to ease re-ordering
   *          later.
   * @return A list of blocks, which may just consist of the original paragraph.
   */
  protected List<ASTNode> expandParagraph(Paragraph paragraph, boolean reversed) {
    ASTNode inline = paragraph.getChildren().get(0);
    List<ASTNode> chunks = inline.getChildren();
    int numchunks = chunks.size();

    // Drop empty inlines, as that means we've examined an entire paragraph.
    if (numchunks == 0) {
      return new ArrayList<ASTNode>();
    }

    ASTNode head = chunks.get(0);
    String sep = (numchunks > 1) ? chunks.get(1).toXHTML() : "\r\n";
    List<ASTNode> tail = (numchunks > 1) ? chunks.subList(1, numchunks) : new ArrayList<ASTNode>();
    Paragraph rest = new Paragraph(new Inline(tail));
    List<ASTNode> out = new ArrayList<ASTNode>();

    // Drop leading whitespace
    if (head.toXHTML().matches("^\r?\n$")) {
      return expandParagraph(rest, reversed);
    }

    // Only continue if there is a hope of expanding it
    if (head instanceof InlineCode || head instanceof MacroNode) {
      // Check if we have a valid separator
      ASTNode block = null;
      if (!reversed && (sep.startsWith("\r\n") || sep.startsWith("\n")) || (reversed && sep.endsWith("\n")) || sep.startsWith("<br")) {
        if (head instanceof InlineCode) {
          block = ((InlineCode) head).toBlock();
        }
        else if (head instanceof MacroNode) {
          block = head;
        }
      }

      // Check if we have a match, and build the result list.
      if (block != null) {
        out.add(block);
        out.addAll(expandParagraph(rest, reversed));
        return out;
      }
    }

    if (reversed) {
      List<ASTNode> rchunks = new ArrayList<ASTNode>(inline.getChildren());
      Collections.reverse(rchunks);
      out.add(new Paragraph(new Inline(rchunks)));
    }
    else {
      out.add(paragraph);
    }
    return out;
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

      // If we have a paragraph, rip off initial and trailing inline code and
      // macros.
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

        continue;
      }

      // Otherwise, just add the block to the list.
      blocks.add(ren);
    }

    // Remove paragraphs just consisting of whitespace
    List<ASTNode> blocksNonEmpty = new ArrayList<ASTNode>();

    for (ASTNode block : blocks) {
      if (!(block instanceof Paragraph && block.getChildren().get(0).toXHTML().trim().equals(""))) {
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
  public ASTNode visitHeading(HeadingContext ctx) {
    if(ctx.inline() == null) return new Plaintext(ctx.HSt().getText());
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
  }

  /**
   * Render a paragraph node. This consists of a single inline element.
   */
  @Override
  public ASTNode visitParagraph(ParagraphContext ctx) {
    return new Paragraph(visit(ctx.inline()));
  }

  /**
   * Render an inline node. This consists of a list of chunks of smaller markup
   * units, which are displayed in order.
   */
  @Override
  public ASTNode visitInline(InlineContext ctx) {
    List<ASTNode> chunks = new ArrayList<ASTNode>();

    // Merge adjacent Any nodes into long Plaintext nodes, to give a more useful
    // AST.
    ASTNode last = null;
    for (InlinestepContext itx : ctx.inlinestep()) {
      ASTNode rendered = visit(itx);
      if (last == null) {
        last = rendered;
      }
      else {
        if (last instanceof Plaintext && rendered instanceof Plaintext) {
          last = new Plaintext(((Plaintext) last).getText() + ((Plaintext) rendered).getText());
        }
        else {
          chunks.add(last);
          last = rendered;
        }
      }
    }

    if (last != null) {
      chunks.add(last);
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
   * Render an attachment link. If the attachment doesn't exist, it is rendered
   * in plain text.
   */
  @Override
  public ASTNode visitAttachment(AttachmentContext ctx) {
    if (store != null) {
      // Check if the attachment exists
      try {
        for (AttachmentHistory attachment : store.attachments(page)) {
          // Skip deleted attachments
          if (attachment.isAttachmentDeleted())
            continue;

          // Render the link if the name matches
          if (attachment.getName().equals(ctx.getText())) {
            return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, linkHandler);
          }
        }
      }
      catch (PageStoreException e) {
      }
    }
    return new Plaintext(ctx.getText());
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
    String target = ctx.InLink(0).getText();
    String title = (ctx.InLink().size() == 1) ? target : ctx.InLink(1).getText();
    return new Link(target, title, page, urlOutputFilter, linkHandler);
  }

  /**
   * Render an image node.
   */
  @Override
  public ASTNode visitImglink(ImglinkContext ctx) {
    String target = ctx.InLink(0).getText();
    String title = (ctx.InLink().size() == 1) ? target : ctx.InLink(1).getText();
    return new Image(target, title, page, urlOutputFilter, imageHandler);
  }

  /**
   * Render an image without a title.
   */
  @Override
  public ASTNode visitSimpleimg(SimpleimgContext ctx) {
    return new Image(ctx.InLink().getText(), ctx.InLink().getText(), page, urlOutputFilter, imageHandler);
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
    ListType type = (ctx.list2().isEmpty() || ctx.list2().get(0).ulist2() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist2(Olist2Context ctx) {
    ListType type = (ctx.list3().isEmpty() || ctx.list3().get(0).ulist3() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist3(Olist3Context ctx) {
    ListType type = (ctx.list4().isEmpty() || ctx.list4().get(0).ulist4() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist4(Olist4Context ctx) {
    ListType type = (ctx.list5().isEmpty() || ctx.list5().get(0).ulist5() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist5(Olist5Context ctx) {
    ListType type = (ctx.list6().isEmpty() || ctx.list6().get(0).ulist6() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list6(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist6(Olist6Context ctx) {
    ListType type = (ctx.list7().isEmpty() || ctx.list7().get(0).ulist7() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list7(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist7(Olist7Context ctx) {
    ListType type = (ctx.list8().isEmpty() || ctx.list8().get(0).ulist8() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list8(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist8(Olist8Context ctx) {
    ListType type = (ctx.list9().isEmpty() || ctx.list9().get(0).ulist9() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list9(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist9(Olist9Context ctx) {
    ListType type = (ctx.list10().isEmpty() || ctx.list10().get(0).ulist10() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list10(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitOlist10(Olist10Context ctx) {
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
    ListType type = (ctx.list2().isEmpty() || ctx.list2().get(0).ulist2() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list2(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist2(Ulist2Context ctx) {
    ListType type = (ctx.list3().isEmpty() || ctx.list3().get(0).ulist3() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list3(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist3(Ulist3Context ctx) {
    ListType type = (ctx.list4().isEmpty() || ctx.list4().get(0).ulist4() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list4(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist4(Ulist4Context ctx) {
    ListType type = (ctx.list5().isEmpty() || ctx.list5().get(0).ulist5() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list5(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist5(Ulist5Context ctx) {
    ListType type = (ctx.list6().isEmpty() || ctx.list6().get(0).ulist6() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list6(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist6(Ulist6Context ctx) {
    ListType type = (ctx.list7().isEmpty() || ctx.list7().get(0).ulist7() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list7(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist7(Ulist7Context ctx) {
    ListType type = (ctx.list8().isEmpty() || ctx.list8().get(0).ulist8() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list8(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist8(Ulist8Context ctx) {
    ListType type = (ctx.list9().isEmpty() || ctx.list9().get(0).ulist9() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list9(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist9(Ulist9Context ctx) {
    ListType type = (ctx.list10().isEmpty() || ctx.list10().get(0).ulist10() == null) ? ListType.Ordered : ListType.Unordered;
    return renderListItem(type, ctx.list10(), ctx.olist(), ctx.ulist(), ctx.inline());
  }

  /** See {@link #visitOlist} */
  @Override
  public ASTNode visitUlist10(Ulist10Context ctx) {
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
   * possible trailing separator.
   */
  @Override
  public ASTNode visitTrow(TrowContext ctx) {
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
    // If there are no arguments, it's not a macro
    if (ctx.MacroEndNoArgs() != null) {
      return new Plaintext("<<" + ctx.MacroName().getText() + ">>");
    }
    return new MacroNode(ctx.MacroName().getText(), cutOffEndTag(ctx.MacroEnd(), ">>"), store, page, urlOutputFilter, linkHandler, imageHandler);
  }
}
