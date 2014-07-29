package net.hillsdon.reviki.wiki.renderer.creole.ast;

/**
 * Interface for inline AST nodes which can be promoted to block elements.
 *
 * @author msw
 */
public interface BlockableNode<T extends ASTNode> {
  
  /**
   * Construct a block-level version of this element. This MAY NOT mutate the
   * current object.
   *
   * @return A block-level version of this element.
   */
  public abstract T toBlock();
}
