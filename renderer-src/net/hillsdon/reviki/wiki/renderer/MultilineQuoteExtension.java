package net.hillsdon.reviki.wiki.renderer;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.node.Block;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.parser.Parser.ParserExtension;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlRenderer.HtmlRendererExtension;
import org.commonmark.renderer.html.HtmlWriter;

public class MultilineQuoteExtension implements HtmlRendererExtension, ParserExtension {

  public static class BlockNode extends CustomBlock {
  }

  public static class BlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_START_END = Pattern.compile(">>>\\s*");

    private boolean _isFinished = false;

    private final BlockNode _node = new BlockNode();

    private BlockParser() {
    }

    public static class Factory extends AbstractBlockParserFactory {
      @Override
      public BlockStart tryStart(final ParserState state, final MatchedBlockParser matchedBlockParser) {
        CharSequence line = state.getLine();
        if (REGEX_START_END.matcher(line).matches()) {
          return BlockStart.of(new BlockParser()).atIndex(line.length());
        }
        return BlockStart.none();
      }
    }

    @Override
    public Block getBlock() {
      return _node;
    }

    @Override
    public boolean isContainer() {
      return true;
    }

    @Override
    public boolean canContain(final Block block) {
      // No nested quote extensions
      return !(block instanceof BlockNode);
    }

    @Override
    public BlockContinue tryContinue(final ParserState state) {
      CharSequence line = state.getLine();
      // Horrible horrible hack to get around the fact you can't skip and break an outer block
      if (_isFinished) {
        return BlockContinue.none();
      }
      if (REGEX_START_END.matcher(line.subSequence(state.getNextNonSpaceIndex(), line.length())).matches()) {
        _isFinished = true;
        return BlockContinue.atIndex(line.length());
      }
      return BlockContinue.atColumn(0);
    }
  }

  public static class Renderer implements NodeRenderer {
    private final HtmlNodeRendererContext _context;
    private final HtmlWriter _writer;

    public Renderer(final HtmlNodeRendererContext context) {
      _context = context;
      _writer = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
      return Collections.<Class<? extends Node>>singleton(BlockNode.class);
    }

    @Override
    public void render(final Node node) {
      _writer.line();
      _writer.tag("blockquote");
      renderChildren(node);
      _writer.tag("/blockquote");
      _writer.line();
    }

    private void renderChildren(final Node parent) {
      Node node = parent.getFirstChild();
      while (node != null) {
        Node next = node.getNext();
        _context.render(node);
        node = next;
      }
    }
  }

  private MultilineQuoteExtension() {
  }

  public static Extension create() {
    return new MultilineQuoteExtension();
  }

  @Override
  public void extend(final Parser.Builder builder) {
    builder.customBlockParserFactory(new BlockParser.Factory());
  }

  @Override
  public void extend(final HtmlRenderer.Builder builder) {
    builder.nodeRendererFactory(new HtmlNodeRendererFactory() {
      @Override
      public NodeRenderer create(final HtmlNodeRendererContext context) {
          return new Renderer(context);
      }
    });
  }

}
