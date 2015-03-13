package net.hillsdon.reviki.wiki.renderer.creole;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.SimpleAttachmentHistory;
import net.hillsdon.reviki.vc.SimplePageStore;
import net.hillsdon.reviki.wiki.renderer.creole.Creole.*;
import net.hillsdon.reviki.wiki.renderer.creole.ast.*;

/**
 * Helper class providing some useful functions for walking through the Creole
 * parse tree and building an AST.
 *
 * @author msw
 */
public abstract class CreoleASTBuilder extends CreoleBaseVisitor<ASTNode> {
  /** The page store. */
  private final Optional<SimplePageStore> _store;

  /** The page being rendered. */
  private final PageInfo _page;

  /** The URL handler for links. */
  private final LinkPartsHandler _linkHandler;

  /** The URL handler for images. */
  private final LinkPartsHandler _imageHandler;

  /** List of attachments on the page. */
  private Collection<? extends SimpleAttachmentHistory> _attachments = null;

  public Optional<SimplePageStore> store() {
    return _store;
  }

  public SimplePageStore unsafeStore() {
    return _store.get();
  }

  public PageInfo page() {
    return _page;
  }

  public LinkPartsHandler linkHandler() {
    return _linkHandler;
  }

  public LinkPartsHandler imageHandler() {
    return _imageHandler;
  }

  /**
   * Aggregate results produced by visitChildren. This returns the "right"most
   * non-null result found.
   */
  @Override
  protected ASTNode aggregateResult(final ASTNode aggregate, final ASTNode nextResult) {
    return (nextResult == null) ? aggregate : nextResult;
  }

  /**
   * Construct a new AST builder.
   *
   * @param store The page store.
   * @param page The page being rendered.
   * @param handler The URL renderer
   */
  private CreoleASTBuilder(final Optional<SimplePageStore> store, final PageInfo page, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    _store = store;
    _page = page;
    _linkHandler = linkHandler;
    _imageHandler = imageHandler;
  }

  /** Construct a new AST builder with a page store. */
  public CreoleASTBuilder(final SimplePageStore store, final PageInfo page, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    this(Optional.of(store), page, linkHandler, imageHandler);
  }

  /** Construct a new AST builder without a page store. */
  public CreoleASTBuilder(final PageInfo page, final LinkPartsHandler linkHandler, final LinkPartsHandler imageHandler) {
    this(Optional.<SimplePageStore> absent(), page, linkHandler, imageHandler);
  }

  /**
   * Render some inline markup (like bold, italic, or strikethrough) by
   * converting the opening symbol into a plaintext AST node if the closing
   * symbol was missing.
   *
   * If italics is "//", this lets us render things like "//foo bar" as
   * "//foo bar" rather than, as ANTLR's error recovery would produce, "
   * <em>foo bar</em>".
   *
   * @param symbol The opening/closing symbol.
   * @param sname The name of the ending symbol
   * @param type The AST node class to use on success
   * @param end The (possibly missing) ending token.
   * @param inline The contents of the markup
   * @return Either an instance of the marked-up node if the ending token is
   *         present, or an inline node consisting of a plaintext start token
   *         followed by the rest of the rendered content.
   */
  protected ASTNode renderInlineMarkup(final Class<? extends ASTNode> type, final String symbol, final String sname, final TerminalNode end, final InlineContext inline) {
    ASTNode inner = (inline != null) ? visit(inline) : new Plaintext("");
    ASTNode innerNoTrim = (inline != null) ? visitInlineNoTrim(inline) : new Plaintext("");

    // If the end tag is missing, undo the error recovery
    if (end == null || ("<missing " + sname + ">").equals(end.getText())) {
      return new Inline(ImmutableList.of((ASTNode) new Plaintext(symbol)));
    }

    // If the inner text is missing, this is not markup
    if (innerNoTrim.toSmallString().equals("")) {
      return new Inline(ImmutableList.of((ASTNode) new Plaintext(symbol + symbol)));
    }

    try {
      Constructor<? extends ASTNode> constructor = type.getConstructor(ASTNode.class);
      return constructor.newInstance(inner.toSmallString().equals("") ? innerNoTrim : inner);
    }
    catch (Exception e) {
      // Never reached if you pass in correct params
      // TODO: Get rid of this (a lambda to construct, rather than reflection,
      // would work)
      throw new RuntimeException(e);
    }
  }

  /**
   * Trim whitespace of an inline node.
   */
  public Inline trimInline(final Inline inline) {
    List<ASTNode> chunks = new ArrayList<ASTNode>();
    chunks.addAll(inline.getChildren());

    // Left-trim the first chunk if it's plaintext
    int sz = chunks.size();
    if (sz > 0 && chunks.get(0) instanceof Plaintext) {
      Plaintext trimmed = new Plaintext(((Plaintext) chunks.get(0)).getText().replaceAll("^\\s+", ""));

      if (trimmed.getText().equals("")) {
        chunks.remove(0);
        sz = chunks.size();
      }
      else {
        chunks.set(0, trimmed);
      }
    }

    // Right-trim the last chunk if it's plaintext
    if (sz > 0 && chunks.get(sz - 1) instanceof Plaintext) {
      Plaintext trimmed = new Plaintext(((Plaintext) chunks.get(sz - 1)).getText().replaceAll("\\s+$", ""));

      if (trimmed.getText().equals("")) {
        chunks.remove(sz - 1);
      }
      else {
        chunks.set(sz - 1, trimmed);
      }
    }

    return new Inline(chunks);
  }

  /**
   * Render an inline node with no whitespace trimming.
   */
  public Inline visitInlineNoTrim(final InlineContext ctx) {
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
          last = ((Plaintext) last).append(((Plaintext) rendered));
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
   * Remove the ending tag (possibly followed by trailing whitespace) from a
   * string.
   *
   * @param node The body + ending tag.
   * @param end The ending tag text.
   * @return The body of the tag.
   */
  protected String cutOffEndTag(final TerminalNode node, final String end) {
    String res = node.getText().replaceAll("\\s+$", "");
    return res.substring(0, res.length() - end.length());
  }

  /**
   * Find the language type from a CodeStart token
   */
  protected String findLanguageType(final TerminalNode node) {
    return node.getText().toUpperCase().replace("+", "PLUS");
  }

  /**
   * Class to hold the context of a list item to render.
   */
  protected static final class ListItemContext {
    private final ParserRuleContext _ordered;

    private final ParserRuleContext _unordered;

    public ListItemContext(final ParserRuleContext ordered, final ParserRuleContext unordered) {
      _ordered = ordered;
      _unordered = unordered;
    }

    public ParserRuleContext get() {
      return ordered() ? _ordered : _unordered;
    }

    public boolean ordered() {
      return _ordered != null;
    }
  }

  /** Types of list contexts */
  protected enum ListCtxType {
    List2Context, List3Context, List4Context, List5Context, List6Context, List7Context, List8Context, List9Context, List10Context
  };

  /** Get the ordered component of a list context. */
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

  /** Get the unordered component of a list context. */
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

  /**
   * Render a list item.
   *
   * @param childContexts List of child elements
   * @param inner List of inner elements.
   * @return A list item containing with the given child list elements.
   */
  protected ASTNode renderListItem(final List<? extends ParserRuleContext> childContexts, final InListContext inner) {
    List<ASTNode> parts = new ArrayList<ASTNode>();

    for (ParserRuleContext in : inner.listBlock()) {
      if (in != null) {
        parts.add(visit(in));
      }
    }

    if (!childContexts.isEmpty()) {
      // In general, a list can contain any arbitrary combination of ordered and
      // unordered sublists, and we want to preserve the (un)orderedness in the
      // bullet points, so we have to render them all individually.
      Boolean isOrdered = null;
      List<ASTNode> items = new ArrayList<ASTNode>();

      // Build lists of (un)ordered chunks one at a time, rendering them, and
      // adding to the list of lists.
      for (ParserRuleContext ctx : childContexts) {
        ListItemContext child = new ListItemContext(olist(ctx), ulist(ctx));

        if (isOrdered != null && child.ordered() != isOrdered.booleanValue()) {
          parts.add(isOrdered.booleanValue() ? new OrderedList(items) : new UnorderedList(items));
          items.clear();
        }

        isOrdered = Boolean.valueOf(child.ordered());
        items.add(visit(child.get()));
      }

      if (!items.isEmpty()) {
        parts.add(isOrdered.booleanValue() ? new OrderedList(items) : new UnorderedList(items));
      }
    }

    return new ListItem(parts);
  }

  /**
   * Check if the page has the named attachment.
   *
   * @param name Filename of the attachment.
   */
  protected boolean hasAttachment(final String name) {
    // If there's no store, say we have no attachments.
    if (!store().isPresent()) {
      return false;
    }

    try {
      // Cache the attachments list
      if (_attachments == null) {
        _attachments = unsafeStore().listAttachments(page());
      }

      // Read through the list.
      for (SimpleAttachmentHistory attachment : _attachments) {
        if (!attachment.isAttachmentDeleted() && attachment.getName().equals(name)) {
          return true;
        }
      }
    }
    catch (PageStoreException e) {
    }

    return false;
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
    String sep = (numchunks > 1) ? chunks.get(1).toSmallString() : "\r\n";
    List<ASTNode> tail = (numchunks > 1) ? chunks.subList(1, numchunks) : new ArrayList<ASTNode>();
    Paragraph rest = new Paragraph(new Inline(tail));
    List<ASTNode> out = new ArrayList<ASTNode>();

    // Drop leading whitespace
    if (head.toSmallString().matches("^\r?\n$")) {
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
}
