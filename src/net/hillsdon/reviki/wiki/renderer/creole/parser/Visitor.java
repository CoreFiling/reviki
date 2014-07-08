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
  public RenderNode visitHeading1(Heading1Context ctx) {
    return new Heading(1, visit(ctx.inline()));
  }

  @Override
  public RenderNode visitHeading2(Heading2Context ctx) {
    return new Heading(2, visit(ctx.inline()));
  }

  @Override
  public RenderNode visitHeading3(Heading3Context ctx) {
    return new Heading(3, visit(ctx.inline()));
  }

  @Override
  public RenderNode visitHeading4(Heading4Context ctx) {
    return new Heading(4, visit(ctx.inline()));
  }

  @Override
  public RenderNode visitHeading5(Heading5Context ctx) {
    return new Heading(5, visit(ctx.inline()));
  }

  @Override
  public RenderNode visitHeading6(Heading6Context ctx) {
    return new Heading(6, visit(ctx.inline()));
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
    return new InlineNoWiki(ctx.AnyInlineText().getText());
  }

  @Override
  public RenderNode visitLinebreak(LinebreakContext ctx) {
    return new Linebreak();
  }
}
