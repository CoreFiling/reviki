package net.hillsdon.reviki.wiki.renderer.creole.ast;

/**
 * Provides a visitor interface to an AST.
 *
 * @author msw
 */
public abstract class ASTVisitor<T> {
  /** Enum of node types. */
  public static enum NodeTypes {
    ASTNode, Anchor,
    BlockableNode, Blockquote, Bold,
    Code, Nowiki, InlineNowiki,
    DirectiveNode,
    Heading, HorizontalRule,
    Image, Inline, InlineCode, Italic,
    Linebreak, Link, LinkNode, ListItem,
    MacroNode,
    OrderedList,
    Page, Paragraph, Plaintext,
    Raw,
    Strikethrough,
    Table, TableCell, TableHeaderCell, TableRow, TextNode,
    UnorderedList;
  }

  /**
   * Visit an arbitrary AST node. This calls the method visit + classname if it
   * exists, and falls back to visitASTNode if not.
   *
   * @param node The node to visit.
   */
  public T visit(final ASTNode node) {
    // This doesn't include the abstract node types, as they can't be instantiated.
    switch(NodeTypes.valueOf(node.getClass().getSimpleName())) {
      case Anchor:          return visitAnchor((Anchor) node);
      case Blockquote:      return visitBlockquote((Blockquote) node);
      case Bold:            return visitBold((Bold) node);
      case Code:            return visitCode((Code) node);
      case Nowiki:          return visitNowiki((Nowiki) node);
      case DirectiveNode:   return visitDirectiveNode((DirectiveNode) node);
      case Heading:         return visitHeading((Heading) node);
      case HorizontalRule:  return visitHorizontalRule((HorizontalRule) node);
      case Image:           return visitImage((Image) node);
      case Inline:          return visitInline((Inline) node);
      case InlineCode:      return visitInlineCode((InlineCode) node);
      case InlineNowiki:    return visitInlineNowiki((InlineNowiki) node);
      case Italic:          return visitItalic((Italic) node);
      case Linebreak:       return visitLinebreak((Linebreak) node);
      case Link:            return visitLink((Link) node);
      case ListItem:        return visitListItem((ListItem) node);
      case MacroNode:       return visitMacroNode((MacroNode) node);
      case OrderedList:     return visitOrderedList((OrderedList) node);
      case Page:            return visitPage((Page) node);
      case Paragraph:       return visitParagraph((Paragraph) node);
      case Plaintext:       return visitPlaintext((Plaintext) node);
      case Raw:             return visitRaw((Raw) node);
      case Strikethrough:   return visitStrikethrough((Strikethrough) node);
      case Table:           return visitTable((Table) node);
      case TableCell:       return visitTableCell((TableCell) node);
      case TableHeaderCell: return visitTableHeaderCell((TableHeaderCell) node);
      case TableRow:        return visitTableRow((TableRow) node);
      case UnorderedList:   return visitUnorderedList((UnorderedList) node);

      // This is included as a fallback, as people might define their own ASTNode
      // subtypes not covered here.
      default: return visitASTNode(node);
    }
  }

  /**
   * Visit an AST node. This is the one mandatory method of ASTVisitor.
   *
   * @param node The node to visit.
   */
  public abstract T visitASTNode(final ASTNode node);

  /** Regular nodes. */
  public T visitAnchor(Anchor node)                         { return visitASTNode(node); }
  public T visitBlockableNode(final BlockableNode node)     { return visitASTNode((ASTNode) node); }
  public T visitBlockquote(Blockquote node)                 { return visitASTNode(node); }
  public T visitBold(final Bold node)                       { return visitASTNode(node); }
  public T visitCode(final Code node)                       { return visitASTNode(node); }
  public T visitNowiki(final Nowiki node)                   { return visitASTNode(node); }
  public T visitDirectiveNode(final DirectiveNode node)     { return visitASTNode(node); }
  public T visitHeading(final Heading node)                 { return visitASTNode(node); }
  public T visitHorizontalRule(final HorizontalRule node)   { return visitASTNode(node); }
  public T visitInline(final Inline node)                   { return visitASTNode(node); }
  public T visitInlineCode(final InlineCode node)           { return visitASTNode(node); }
  public T visitInlineNowiki(final InlineNowiki node)       { return visitASTNode(node); }
  public T visitItalic(final Italic node)                   { return visitASTNode(node); }
  public T visitLinebreak(final Linebreak node)             { return visitASTNode(node); }
  public T visitListItem(final ListItem node)               { return visitASTNode(node); }
  public T visitMacroNode(final MacroNode node)             { return visitASTNode(node); }
  public T visitOrderedList(final OrderedList node)         { return visitASTNode(node); }
  public T visitPage(final Page node)                       { return visitASTNode(node); }
  public T visitParagraph(final Paragraph node)             { return visitASTNode(node); }
  public T visitStrikethrough(final Strikethrough node)     { return visitASTNode(node); }
  public T visitTable(final Table node)                     { return visitASTNode(node); }
  public T visitTableCell(final TableCell node)             { return visitASTNode(node); }
  public T visitTableHeaderCell(final TableHeaderCell node) { return visitASTNode(node); }
  public T visitTableRow(final TableRow node)               { return visitASTNode(node); }
  public T visitUnorderedList(final UnorderedList node)     { return visitASTNode(node); }

  // Link nodes.
  public T visitImage(final Image node)                     { return visitLinkNode(node); }
  public T visitLink(final Link node)                       { return visitLinkNode(node); }
  public T visitLinkNode(final LinkNode node)               { return visitASTNode(node);  }

  // Text nodes. */
  public T visitPlaintext(final Plaintext node)             { return visitTextNode(node); }
  public T visitRaw(final Raw node)                         { return visitTextNode(node); }
  public T visitTextNode(final TextNode node)               { return visitASTNode(node);  }
}
