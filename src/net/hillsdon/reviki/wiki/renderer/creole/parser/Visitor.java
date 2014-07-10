package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.web.urls.URLOutputFilter;
import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.parser.Creole.*;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.*;
import net.hillsdon.reviki.wiki.renderer.result.ResultNode;

public class Visitor extends CreoleBaseVisitor<ResultNode> {
  private LinkPartsHandler handler;

  private PageInfo page;

  private URLOutputFilter urlOutputFilter;

  public Visitor(final PageInfo page, final URLOutputFilter urlOutputFilter, final LinkPartsHandler handler) {
    this.page = page;
    this.urlOutputFilter = urlOutputFilter;
    this.handler = handler;
  }

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

  @Override
  public ResultNode visitHeading(HeadingContext ctx) {
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
  }

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

    if (inner instanceof InlineNoWiki) {
      return new NoWiki(((InlineNoWiki) inner).getPreformatted());
    }

    return new Paragraph(body);
  }

  @Override
  public ResultNode visitInline(InlineContext ctx) {
    List<ResultNode> chunks = new ArrayList<ResultNode>();

    for (InlinestepContext itx : ctx.inlinestep()) {
      chunks.add(visit(itx));
    }

    return new Inline(chunks);
  }

  @Override
  public ResultNode visitAny(AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  @Override
  public ResultNode visitWikiwlink(WikiwlinkContext ctx) {
    return new Link(ctx.getText(), ctx.getText(), page, urlOutputFilter, handler);
  }

  protected ResultNode visitInlineMarkup(String symbol, String sname, Class<? extends ResultNode> type, TerminalNode end, InlineContext inline) {
    ResultNode inner = (inline != null) ? visit(inline) : new Plaintext("");

    // If the end tag is missing, undo the error recovery
    if (end == null || ("<missing " + sname + ">").equals(end.getText())) {
      List<ResultNode> chunks = new ArrayList<ResultNode>();
      chunks.add(new Plaintext(symbol));
      chunks.add(inner);
      return new Inline(chunks);
    }

    try {
      @SuppressWarnings("unchecked")
      Constructor<ResultNode> constructor = (Constructor<ResultNode>) type.getConstructors()[0];
      return constructor.newInstance(inner);
    }
    catch (Throwable e) {
      // Never reached if you pass in correct params
      return null;
    }
  }

  @Override
  public ResultNode visitBold(BoldContext ctx) {
    return visitInlineMarkup("**", "BEnd", Bold.class, ctx.BEnd(), ctx.inline());
  }

  @Override
  public ResultNode visitItalic(ItalicContext ctx) {
    return visitInlineMarkup("//", "IEnd", Italic.class, ctx.IEnd(), ctx.inline());
  }

  @Override
  public ResultNode visitSthrough(SthroughContext ctx) {
    return visitInlineMarkup("--", "SEnd", Strikethrough.class, ctx.SEnd(), ctx.inline());
  }

  @Override
  public ResultNode visitLink(LinkContext ctx) {
    return new Link(ctx.InLink().getText(), ctx.InLink().getText(), page, urlOutputFilter, handler);
  }

  @Override
  public ResultNode visitTitlelink(TitlelinkContext ctx) {
    return new Link(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, handler);
  }

  @Override
  public ResultNode visitImglink(ImglinkContext ctx) {
    return new Image(ctx.InLink(0).getText(), ctx.InLink(1).getText(), page, urlOutputFilter, handler);
  }

  @Override
  public ResultNode visitPreformat(PreformatContext ctx) {
    String nowiki = ctx.EndNoWikiInline().getText();
    return new InlineNoWiki(nowiki.substring(0, nowiki.length() - 3));
  }

  @Override
  public ResultNode visitLinebreak(LinebreakContext ctx) {
    return new Linebreak();
  }

  @Override
  public ResultNode visitHrule(HruleContext ctx) {
    return new HorizontalRule();
  }

  @Override
  public ResultNode visitOlist(OlistContext ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Olist1Context otx : ctx.olist1()) {
      children.add(visit(otx));
    }

    return new OrderedList(new Plaintext(""), children);
  }

  @Override
  public ResultNode visitOlist1(Olist1Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Olist2Context otx : ctx.olist2()) {
      children.add(visit(otx));
    }

    if (ctx.olist() != null)
      return new OrderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new OrderedList(visit(ctx.ulist()), children);

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitOlist2(Olist2Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Olist3Context otx : ctx.olist3()) {
      children.add(visit(otx));
    }

    if (ctx.olist() != null)
      return new OrderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new OrderedList(visit(ctx.ulist()), children);

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitOlist3(Olist3Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Olist4Context otx : ctx.olist4()) {
      children.add(visit(otx));
    }

    if (ctx.olist() != null)
      return new OrderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new OrderedList(visit(ctx.ulist()), children);

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitOlist4(Olist4Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Olist5Context otx : ctx.olist5()) {
      children.add(visit(otx));
    }

    if (ctx.olist() != null)
      return new OrderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new OrderedList(visit(ctx.ulist()), children);

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitOlist5(Olist5Context ctx) {

    if (ctx.olist() != null)
      return new OrderedList(visit(ctx.olist()), new ArrayList<ResultNode>());

    if (ctx.ulist() != null)
      return new OrderedList(visit(ctx.ulist()), new ArrayList<ResultNode>());

    return new OrderedList(visit(ctx.inline()), new ArrayList<ResultNode>());
  }

  @Override
  public ResultNode visitUlist(UlistContext ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Ulist1Context otx : ctx.ulist1()) {
      children.add(visit(otx));
    }

    return new UnorderedList(new Plaintext(""), children);
  }

  @Override
  public ResultNode visitUlist1(Ulist1Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Ulist2Context utx : ctx.ulist2()) {
      children.add(visit(utx));
    }

    if (ctx.olist() != null)
      return new UnorderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new UnorderedList(visit(ctx.ulist()), children);

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitUlist2(Ulist2Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Ulist3Context utx : ctx.ulist3()) {
      children.add(visit(utx));
    }

    if (ctx.olist() != null)
      return new UnorderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new UnorderedList(visit(ctx.ulist()), children);

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitUlist3(Ulist3Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Ulist4Context utx : ctx.ulist4()) {
      children.add(visit(utx));
    }

    if (ctx.olist() != null)
      return new UnorderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new UnorderedList(visit(ctx.ulist()), children);

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitUlist4(Ulist4Context ctx) {
    List<ResultNode> children = new ArrayList<ResultNode>();

    for (Ulist5Context utx : ctx.ulist5()) {
      children.add(visit(utx));
    }

    if (ctx.olist() != null)
      return new UnorderedList(visit(ctx.olist()), children);

    if (ctx.ulist() != null)
      return new UnorderedList(visit(ctx.ulist()), children);

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public ResultNode visitUlist5(Ulist5Context ctx) {
    if (ctx.olist() != null)
      return new UnorderedList(visit(ctx.olist()), new ArrayList<ResultNode>());

    if (ctx.ulist() != null)
      return new UnorderedList(visit(ctx.ulist()), new ArrayList<ResultNode>());

    return new UnorderedList(visit(ctx.inline()), new ArrayList<ResultNode>());
  }

  @Override
  public ResultNode visitNowiki(NowikiContext ctx) {
    String nowiki = ctx.EndNoWikiBlock().getText();
    return new NoWiki(nowiki.substring(0, nowiki.length() - 3));
  }

  @Override
  public ResultNode visitTable(TableContext ctx) {
    List<ResultNode> rows = new ArrayList<ResultNode>();

    for (TrowContext rtx : ctx.trow()) {
      rows.add(visit(rtx));
    }

    return new Table(rows);
  }

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

  @Override
  public ResultNode visitTh(ThContext ctx) {
    return new TableHeaderCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  @Override
  public ResultNode visitTd(TdContext ctx) {
    return new TableCell((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }
}
