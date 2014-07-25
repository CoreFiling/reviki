package net.hillsdon.reviki.wiki.renderer.creole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;
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
  /** List of attachments on the page. */
  private Collection<AttachmentHistory> _attachments = null;

  public Visitor(final Optional<PageStore> store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super(store, page, urlOutputFilter, linkHandler, imageHandler);
  }

  public Visitor(final PageStore store, final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super(store, page, urlOutputFilter, linkHandler, imageHandler);
  }

  public Visitor(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    super(page, urlOutputFilter, linkHandler, imageHandler);
  }

  /**
   * If a paragraph starts with a sequence of blockable elements, separated by
   * newlines, render them as blocks and the remaining text (if any) as a
   * paragraph following this.
   *
   * @param paragraph The paragraph to expand out.
   * @param reversed Whether the paragraph is reversed or not. If so, the final
   *          trailing paragraph has its chunks reversed, to ease re-ordering
   *          later.
   * @return A list of blocks, which may just consist of the original paragraph.
   */
  protected List<ASTNode> expandParagraph(final Paragraph paragraph, final boolean reversed) {
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
    if (head instanceof BlockableNode) {
      // Check if we have a valid separator
      ASTNode block = null;
      if (sep == null || (!reversed && (sep.startsWith("\r\n") || sep.startsWith("\n")) || (reversed && sep.endsWith("\n")) || sep.startsWith("<br"))) {
        block = ((BlockableNode) head).toBlock();
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
      if (block != null && !(block instanceof Paragraph && ((Paragraph) block).innerXHTML().trim().equals(""))) {
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
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
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
  public ASTNode visitAny(final AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  /**
   * Render a WikiWords link.
   */
  @Override
  public ASTNode visitWikiwlink(final WikiwlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page(), urlOutputFilter(), linkHandler());
  }

  /**
   * Render an attachment link. If the attachment doesn't exist, it is rendered
   * in plain text.
   */
  @Override
  public ASTNode visitAttachment(final AttachmentContext ctx) {
    if (store().isPresent()) {
      // Check if the attachment exists
      try {
        if (_attachments == null) {
          _attachments = unsafeStore().attachments(page());
        }
        for (AttachmentHistory attachment : _attachments) {
          // Skip deleted attachments
          if (attachment.isAttachmentDeleted()) {
            continue;
          }

          // Render the link if the name matches
          if (attachment.getName().equals(ctx.getText())) {
            return new Link(ctx.getText(), ctx.getText(), page(), urlOutputFilter(), linkHandler());
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
  public ASTNode visitRawlink(final RawlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page(), urlOutputFilter(), linkHandler());
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
    return new Link(ctx.InLink().getText(), ctx.InLink().getText(), page(), urlOutputFilter(), linkHandler());
  }

  /**
   * Render a link node with a title.
   */
  @Override
  public ASTNode visitTitlelink(final TitlelinkContext ctx) {
    String target = (ctx.InLink() == null) ? "" : ctx.InLink().getText();
    String title = (ctx.InLinkEnd() == null) ? target : ctx.InLinkEnd().getText();
    return new Link(target, title, page(), urlOutputFilter(), linkHandler());
  }

  /**
   * Render an image node.
   */
  @Override
  public ASTNode visitImglink(final ImglinkContext ctx) {
    String target = (ctx.InLink() == null) ? "" : ctx.InLink().getText();
    String title = (ctx.InLinkEnd() == null) ? target : ctx.InLinkEnd().getText();
    return new Image(target, title, page(), urlOutputFilter(), imageHandler());
  }

  /**
   * Render an image without a title.
   */
  @Override
  public ASTNode visitSimpleimg(final SimpleimgContext ctx) {
    return new Image(ctx.InLink().getText(), ctx.InLink().getText(), page(), urlOutputFilter(), imageHandler());
  }

  /**
   * Render an inline nowiki node. Due to how the lexer works, the contents
   * include the ending symbol, which must be chopped off.
   *
   * TODO: Improve the tokensiation of this.
   */
  @Override
  public ASTNode visitPreformat(final PreformatContext ctx) {
    return renderInlineCode(ctx.EndNoWikiInline(), "}}}");
  }

  /**
   * Render a syntax-highlighted CPP block. This has the same tokenisation
   * problem as mentioned in {@link #visitPreformat}.
   */
  @Override
  public ASTNode visitInlinecpp(final InlinecppContext ctx) {
    return renderInlineCode(ctx.EndCppInline(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitInlinehtml(final InlinehtmlContext ctx) {
    String code = ctx.EndHtmlInline().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode}. */
  @Override
  public ASTNode visitInlinejava(final InlinejavaContext ctx) {
    return renderInlineCode(ctx.EndJavaInline(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode}. */
  @Override
  public ASTNode visitInlinexhtml(final InlinexhtmlContext ctx) {
    return renderInlineCode(ctx.EndXhtmlInline(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitInlinecpp} and {@link #renderInlineCode}. */
  @Override
  public ASTNode visitInlinexml(final InlinexmlContext ctx) {
    return renderInlineCode(ctx.EndXmlInline(), "[</xml>]", XhtmlRendererFactory.XML);
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
  public ASTNode visitOlist(final OlistContext ctx) {
    return renderList(ListType.Ordered, ctx.olist1());
  }

  /** Helper functions for lists. */
  protected enum ListCtxType {
    List2Context, List3Context, List4Context, List5Context, List6Context, List7Context, List8Context, List9Context, List10Context
  };

  protected ParserRuleContext olist(final ParserRuleContext ctx) {
    switch (ListCtxType.valueOf(ctx.getClass().getSimpleName())) {
      case List2Context:
        return ((List2Context) ctx).olist2();
      case List3Context:
        return ((List3Context) ctx).olist3();
      case List4Context:
        return ((List4Context) ctx).olist4();
      case List5Context:
        return ((List5Context) ctx).olist5();
      case List6Context:
        return ((List6Context) ctx).olist6();
      case List7Context:
        return ((List7Context) ctx).olist7();
      case List8Context:
        return ((List8Context) ctx).olist8();
      case List9Context:
        return ((List9Context) ctx).olist9();
      case List10Context:
        return ((List10Context) ctx).olist10();
      default:
        throw new RuntimeException("Unknown list context type");
    }
  }

  protected ParserRuleContext ulist(final ParserRuleContext ctx) {
    switch (ListCtxType.valueOf(ctx.getClass().getSimpleName())) {
      case List2Context:
        return ((List2Context) ctx).ulist2();
      case List3Context:
        return ((List3Context) ctx).ulist3();
      case List4Context:
        return ((List4Context) ctx).ulist4();
      case List5Context:
        return ((List5Context) ctx).ulist5();
      case List6Context:
        return ((List6Context) ctx).ulist6();
      case List7Context:
        return ((List7Context) ctx).ulist7();
      case List8Context:
        return ((List8Context) ctx).ulist8();
      case List9Context:
        return ((List9Context) ctx).ulist9();
      case List10Context:
        return ((List10Context) ctx).ulist10();
      default:
        throw new RuntimeException("Unknown list context type");
    }
  }

  protected ASTNode list(final List<? extends ParserRuleContext> ltxs, final InListContext inner) {
    List<ListItemContext> children = new ArrayList<ListItemContext>();

    if (ltxs != null) {
      for (ParserRuleContext ltx : ltxs) {
        children.add(new ListItemContext(olist(ltx), ulist(ltx)));
      }
    }

    List<ParserRuleContext> inners = new ArrayList<ParserRuleContext>();
    inners.add((ParserRuleContext) inner.olist());
    inners.add((ParserRuleContext) inner.ulist());
    inners.add((ParserRuleContext) inner.inline());

    return renderListItem(children, inners);
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist1(final Olist1Context ctx) {
    return list(ctx.list2(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist2(final Olist2Context ctx) {
    return list(ctx.list3(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist3(final Olist3Context ctx) {
    return list(ctx.list4(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist4(final Olist4Context ctx) {
    return list(ctx.list5(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist5(final Olist5Context ctx) {
    return list(ctx.list6(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist6(final Olist6Context ctx) {
    return list(ctx.list7(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist7(final Olist7Context ctx) {
    return list(ctx.list8(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist8(final Olist8Context ctx) {
    return list(ctx.list9(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist9(final Olist9Context ctx) {
    return list(ctx.list10(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitOlist10(final Olist10Context ctx) {
    return list(null, ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist(final UlistContext ctx) {
    return renderList(ListType.Unordered, ctx.ulist1());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist1(final Ulist1Context ctx) {
    return list(ctx.list2(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist2(final Ulist2Context ctx) {
    return list(ctx.list3(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist3(final Ulist3Context ctx) {
    return list(ctx.list4(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist4(final Ulist4Context ctx) {
    return list(ctx.list5(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist5(final Ulist5Context ctx) {
    return list(ctx.list6(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist6(final Ulist6Context ctx) {
    return list(ctx.list7(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist7(final Ulist7Context ctx) {
    return list(ctx.list8(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist8(final Ulist8Context ctx) {
    return list(ctx.list9(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist9(final Ulist9Context ctx) {
    return list(ctx.list10(), ctx.inList());
  }

  /** See {@link #visitOlist}. */
  @Override
  public ASTNode visitUlist10(final Ulist10Context ctx) {
    return list(null, ctx.inList());
  }

  /**
   * Render a NoWiki block node. This has the same tokenisation problem as
   * mentioned in {@link #visitPreformat}.
   */
  @Override
  public ASTNode visitNowiki(final NowikiContext ctx) {
    return renderBlockCode(ctx.EndNoWikiBlock(), "}}}");
  }

  /**
   * Like {@link #visitInlinecpp}, but for blocks.
   */
  @Override
  public ASTNode visitCpp(final CppContext ctx) {
    return renderBlockCode(ctx.EndCppBlock(), "[</c++>]", XhtmlRendererFactory.CPLUSPLUS);
  }

  /**
   * Render a block of literal, unescaped, HTML.
   */
  @Override
  public ASTNode visitHtml(final HtmlContext ctx) {
    String code = ctx.EndHtmlBlock().getText();
    return new Raw(code.substring(0, code.length() - "[</html>]".length()));
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode}. */
  @Override
  public ASTNode visitJava(final JavaContext ctx) {
    return renderBlockCode(ctx.EndJavaBlock(), "[</java>]", XhtmlRendererFactory.JAVA);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode}. */
  @Override
  public ASTNode visitXhtml(final XhtmlContext ctx) {
    return renderBlockCode(ctx.EndXhtmlBlock(), "[</xhtml>]", XhtmlRendererFactory.XHTML);
  }

  /** See {@link #visitCpp} and {@link #renderBlockCode}. */
  @Override
  public ASTNode visitXml(final XmlContext ctx) {
    return renderBlockCode(ctx.EndXmlBlock(), "[</xml>]", XhtmlRendererFactory.XML);
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
    return new TableHeaderCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  /**
   * Render a table cell.
   */
  @Override
  public ASTNode visitTd(final TdContext ctx) {
    return new TableCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
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
    return new MacroNode(ctx.MacroName().getText(), cutOffEndTag(ctx.MacroEnd(), ">>"), page(), this);
  }
}
