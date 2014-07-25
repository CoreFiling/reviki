package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import net.hillsdon.reviki.wiki.renderer.macro.Macro;

/**
 * A node in the abstract syntax tree produced by the Creole parser.
 *
 * @author msw
 */
public abstract class ASTNode {
  /**
   * The child elements of the node.
   */
  private ImmutableList<ASTNode> _children;

  /**
   * Construct a new AST node.
   *
   * @param children Any child elements of the node.
   */
  public ASTNode(final List<ASTNode> children) {
    _children = ImmutableList.copyOf(children);
  }

  /**
   * Helper method for elements with just one child. See
   * {@link #ASTNode(String, List)}.
   */
  public ASTNode(final ASTNode body) {
    _children = ImmutableList.of(body);
  }

  /**
   * Leaf nodes are fine too.
   */
  public ASTNode() {
    _children = ImmutableList.of();
  }

  /**
   * Return a list of the children of this node. This includes the body (if any)
   * as the first element of the list. This will not be null.
   */
  public ImmutableList<ASTNode> getChildren() {
    return _children;
  }

  /**
   * Produce a valid XHTML representation (assuming valid implementations of
   * toXHTML for all direct and indirect children) of the node.
   */
  public String toXHTML() {
    String out = "";

    for (ASTNode node : getChildren()) {
      out += node.toXHTML();
    }

    return out;
  }

  /**
   * Expand macros contained within this node and its children, returning the
   * modified node.
   *
   * @param macros The list of macros
   * @return The possibly modified node. If the node was not a macro, `this`
   *         will be returned, however if `this` is returned it cannot be
   *         assumed that none of the node's children contained macros.
   */
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    ImmutableList.Builder<ASTNode> adoptees = new ImmutableList.Builder<ASTNode>();

    for (ASTNode child : _children) {
      adoptees.add(child.expandMacros(macros));
    }

    _children = adoptees.build();

    return this;
  }

  /**
   * Produce a pretty tree representation of the AST.
   */
  public final String toStringTree() {
    String out = this.getClass().getSimpleName() + "\n";

    if (getChildren().isEmpty()) {
      return out;
    }

    ASTNode last = getChildren().get(getChildren().size() - 1);
    for (ASTNode node : getChildren()) {
      boolean first = true;
      for (String line : node.toStringTree().split("\n")) {
        if (first) {
          out += (node == last) ? "┗ " : "┣ ";
          first = false;
        }
        else if (node != last) {
          out += "┃ ";
        }
        else {
          out += "  ";
        }
        out += line + "\n";
      }
    }

    return out;
  }
}
