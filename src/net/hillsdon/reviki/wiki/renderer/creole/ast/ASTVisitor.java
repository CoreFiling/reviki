package net.hillsdon.reviki.wiki.renderer.creole.ast;

/**
 * Provides a visitor interface to an AST.
 * 
 * @author msw
 */
public abstract class ASTVisitor<T> {
  /** Enum of node types */
  public static enum NodeTypes {
    ASTNode,
    BlockableNode, Bold,
    Code,
    Heading, HorizontalRule,
    Image, Inline, InlineCode, Italic,
    Linebreak, Link, LinkNode, ListItem,
    MacroNode,
    OrderedList,
    Page, Paragraph, Plaintext,
    Raw,
    Strikethrough,
    Table, TableCell, TableHeaderCell, TableRow, TaggedNode, TextNode,
    UnorderedList;
  }
  /**
   * Visit an arbitrary AST node. This calls the method visit + classname if it
   * exists, and falls back to visitASTNode if not.
   * 
   * @param node The node to visit.
   */
  public T visit(ASTNode node) {
    // This doesn't include the abstract node types, as they can't be instantiated.
    switch(NodeTypes.valueOf(node.getClass().getSimpleName())) {
      case Bold:            return visitBold((Bold) node);
      case Code:            return visitCode((Code) node);
      case Heading:         return visitHeading((Heading) node);
      case HorizontalRule:  return visitHorizontalRule((HorizontalRule) node);
      case Image:           return visitImage((Image) node);
      case Inline:          return visitInline((Inline) node);
      case InlineCode:      return visitInlineCode((InlineCode) node);
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
  public abstract T visitASTNode(ASTNode node);

  public T visitBlockableNode(BlockableNode node)     { return visitASTNode((ASTNode) node); }
  public T visitBold(Bold node)                       { return visitASTNode((ASTNode) node); }
  public T visitCode(Code node)                       { return visitASTNode((ASTNode) node); }
  public T visitHeading(Heading node)                 { return visitASTNode((ASTNode) node); }
  public T visitHorizontalRule(HorizontalRule node)   { return visitASTNode((ASTNode) node); }
  public T visitImage(Image node)                     { return visitASTNode((ASTNode) node); }
  public T visitInline(Inline node)                   { return visitASTNode((ASTNode) node); }
  public T visitInlineCode(InlineCode node)           { return visitASTNode((ASTNode) node); }
  public T visitItalic(Italic node)                   { return visitASTNode((ASTNode) node); }
  public T visitLinebreak(Linebreak node)             { return visitASTNode((ASTNode) node); }
  public T visitLink(Link node)                       { return visitASTNode((ASTNode) node); }
  public T visitLinkNode(LinkNode node)               { return visitASTNode((ASTNode) node); }
  public T visitListItem(ListItem node)               { return visitASTNode((ASTNode) node); }
  public T visitMacroNode(MacroNode node)             { return visitASTNode((ASTNode) node); }
  public T visitOrderedList(OrderedList node)         { return visitASTNode((ASTNode) node); }
  public T visitPage(Page node)                       { return visitASTNode((ASTNode) node); }
  public T visitParagraph(Paragraph node)             { return visitASTNode((ASTNode) node); }
  public T visitPlaintext(Plaintext node)             { return visitASTNode((ASTNode) node); }
  public T visitRaw(Raw node)                         { return visitASTNode((ASTNode) node); }
  public T visitStrikethrough(Strikethrough node)     { return visitASTNode((ASTNode) node); }
  public T visitTable(Table node)                     { return visitASTNode((ASTNode) node); }
  public T visitTableCell(TableCell node)             { return visitASTNode((ASTNode) node); }
  public T visitTableHeaderCell(TableHeaderCell node) { return visitASTNode((ASTNode) node); }
  public T visitTableRow(TableRow node)               { return visitASTNode((ASTNode) node); }
  public T visitTaggedNode(TaggedNode node)           { return visitASTNode((ASTNode) node); }
  public T visitTextNode(TextNode node)               { return visitASTNode((ASTNode) node); }
  public T visitUnorderedList(UnorderedList node)     { return visitASTNode((ASTNode) node); }
}
