package net.hillsdon.reviki.wiki.renderer.creole.parser;

import java.util.ArrayList;
import java.util.List;

import net.hillsdon.reviki.wiki.renderer.creole.LinkPartsHandler;
import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;
import net.hillsdon.reviki.wiki.renderer.creole.parser.ast.*;
import net.hillsdon.reviki.wiki.renderer.creole.parser.Creole.*;

public class Visitor extends CreoleBaseVisitor<RenderNode> {
  private LinkPartsHandler handler;

  public Visitor(final LinkPartsHandler handler) {
    this.handler = handler;
  }

  public RenderNode visitCreole(CreoleContext ctx) {
    List<RenderNode> blocks = new ArrayList<RenderNode>();

    for (BlockContext btx : ctx.block()) {
      RenderNode ren = visit(btx);
      if (ren != null) {
        blocks.add(ren);
      }
    }

    return new Page(blocks);
  }

  @Override
  public RenderNode visitHeading(HeadingContext ctx) {
    return new Heading(ctx.HSt().getText().length(), visit(ctx.inline()));
  }

  @Override
  public RenderNode visitParagraph(ParagraphContext ctx) {
    return new Paragraph(visit(ctx.inline()));
  }

  @Override
  public RenderNode visitInline(InlineContext ctx) {
    List<RenderNode> chunks = new ArrayList<RenderNode>();

    for (InlinestepContext itx : ctx.inlinestep()) {
      chunks.add(visit(itx));
    }

    return new Inline(chunks);
  }

  @Override
  public RenderNode visitAny(AnyContext ctx) {
    return new Plaintext(ctx.getText());
  }

  @Override
  public RenderNode visitWikiwlink(WikiwlinkContext ctx) {
    return new Link(ctx.getText(), handler);
  }

  @Override
  public RenderNode visitBold(BoldContext ctx) {
    return new Bold((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  @Override
  public RenderNode visitItalic(ItalicContext ctx) {
    return new Italic((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  @Override
  public RenderNode visitSthrough(SthroughContext ctx) {
    return new Strikethrough((ctx.inline() != null) ? visit(ctx.inline()) : new Plaintext(""));
  }

  @Override
  public RenderNode visitLink(LinkContext ctx) {
    return new Link(ctx.InLink().getText(), handler);
  }

  @Override
  public RenderNode visitTitlelink(TitlelinkContext ctx) {
    return new Link(ctx.InLink(0).getText(), ctx.InLink(1).getText(), handler);
  }

  @Override
  public RenderNode visitImglink(ImglinkContext ctx) {
    return new Image(ctx.InLink(0).getText(), ctx.InLink(1).getText(), handler);
  }

  @Override
  public RenderNode visitPreformat(PreformatContext ctx) {
    String nowiki = ctx.EndNoWikiInline().getText();
    return new NoWiki(nowiki.substring(0, nowiki.length() - 3));
  }

  @Override
  public RenderNode visitLinebreak(LinebreakContext ctx) {
    return new Linebreak();
  }

  @Override
  public RenderNode visitHrule(HruleContext ctx) {
    return new HorizontalRule();
  }

  @Override
  public RenderNode visitOlist(OlistContext ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Olist1Context otx : ctx.olist1()) {
      children.add(visit(otx));
    }

    return new OrderedList(children);
  }

  @Override
  public RenderNode visitOlist1(Olist1Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Olist2Context otx : ctx.olist2()) {
      children.add(visit(otx));
    }

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitOlist2(Olist2Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Olist3Context otx : ctx.olist3()) {
      children.add(visit(otx));
    }

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitOlist3(Olist3Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Olist4Context otx : ctx.olist4()) {
      children.add(visit(otx));
    }

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitOlist4(Olist4Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Olist5Context otx : ctx.olist5()) {
      children.add(visit(otx));
    }

    return new OrderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitOlist5(Olist5Context ctx) {
    return new OrderedList(visit(ctx.inline()));
  }

  @Override
  public RenderNode visitUlist(UlistContext ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Ulist1Context otx : ctx.ulist1()) {
      children.add(visit(otx));
    }

    return new UnorderedList(children);
  }

  @Override
  public RenderNode visitUlist1(Ulist1Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Ulist2Context utx : ctx.ulist2()) {
      children.add(visit(utx));
    }

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitUlist2(Ulist2Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Ulist3Context utx : ctx.ulist3()) {
      children.add(visit(utx));
    }

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitUlist3(Ulist3Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Ulist4Context utx : ctx.ulist4()) {
      children.add(visit(utx));
    }

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitUlist4(Ulist4Context ctx) {
    List<RenderNode> children = new ArrayList<RenderNode>();

    for (Ulist5Context utx : ctx.ulist5()) {
      children.add(visit(utx));
    }

    return new UnorderedList(visit(ctx.inline()), children);
  }

  @Override
  public RenderNode visitUlist5(Ulist5Context ctx) {
    return new UnorderedList(visit(ctx.inline()));
  }

  @Override
  public RenderNode visitNowiki(NowikiContext ctx) {
    String nowiki = ctx.EndNoWikiBlock().getText();
    return new NoWiki(nowiki.substring(0, nowiki.length() - 3));
  }

  @Override
  public RenderNode visitTable(TableContext ctx) {
    List<RenderNode> rows = new ArrayList<RenderNode>();

    for (TrowContext rtx : ctx.trow()) {
      rows.add(visit(rtx));
    }

    return new Table(rows);
  }

  @Override
  public RenderNode visitTrow(TrowContext ctx) {
    List<RenderNode> cells = new ArrayList<RenderNode>();

    for (TcellContext rtx : ctx.tcell()) {
      cells.add(visit(rtx));
    }

    return new TableRow(cells);
  }

  @Override
  public RenderNode visitTh(ThContext ctx) {
    return new TableHeaderCell(visit(ctx.inline()));
  }

  @Override
  public RenderNode visitTd(TdContext ctx) {
    return new TableCell(visit(ctx.inline()));
  }
}
