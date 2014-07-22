package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

/**
 * Abstract class for inline AST nodes which can be promoted to block elements.
 * 
 * @author msw
 */
public abstract class BlockableNode<T extends ASTNode> extends ASTNode {
  public BlockableNode(String tag, ASTNode body, List<ASTNode> children) {
    super(tag, body, children);
  }

  public BlockableNode(String tag, ASTNode body) {
    super(tag, body);
  }

  public BlockableNode(String tag, List<ASTNode> children) {
    super(tag, children);
  }

  public BlockableNode(String tag) {
    super(tag);
  }

  /**
   * Construct a block-level version of this element. This MAY NOT mutate the
   * current object.
   * 
   * @return A block-level version of this element.
   */
  public abstract T toBlock();
}
