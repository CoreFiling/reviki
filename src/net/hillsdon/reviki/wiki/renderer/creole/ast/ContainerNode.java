package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

/**
 * Abstract class for AST nodes which have no tag or body, and are just a
 * container for other nodes.
 *
 * @author msw
 */
public abstract class ContainerNode<T extends ASTNode> extends ASTNode {
  public ContainerNode(final List<ASTNode> children) {
    super("", null, children);
  }

  @Override
  public String toXHTML() {
    String out = "";

    for (ASTNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }
}
