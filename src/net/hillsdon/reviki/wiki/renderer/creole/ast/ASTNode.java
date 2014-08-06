package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.lang.reflect.Constructor;
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
  private final ImmutableList<ASTNode> _children;

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
   * Expand macros contained within this node and its children, returning the
   * modified node. If no macros were expanded, `this` is returned.
   *
   * This uses reflection to achieve immutability, and so MUST be overridden if
   * the constructor is overridden to have a different signature.
   *
   * @param macros The list of macros
   * @return A node, with macros expanded.
   */
  public ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    // Expand all children
    boolean mutated = false;
    ImmutableList.Builder<ASTNode> adoptees = new ImmutableList.Builder<ASTNode>();

    for (ASTNode child : _children) {
      ASTNode expanded = child.expandMacros(macros);
      adoptees.add(expanded);

      if (expanded != child) {
        mutated = true;
      }
    }

    // If no children were expanded, return `this`.
    if (!mutated) {
      return this;
    }

    // Mutation occurred, let's build a new node.
    ImmutableList<ASTNode> expanded = adoptees.build();

    // Prefer the single-node constructor if it's available and we only have
    // one child.
    if (_children.size() == 1) {
      try {
        Constructor<? extends ASTNode> constructor = getClass().getDeclaredConstructor(ASTNode.class);
        return constructor.newInstance(expanded.get(0));
      }
      catch (Exception e) {
        // The single-node constructor might not be available, but the list
        // one might be, so just ignore this exception and continue.
      }
    }

    try {
      Constructor<? extends ASTNode> constructor = getClass().getDeclaredConstructor(List.class);
      return constructor.newInstance(expanded);
    }
    catch (Exception e) {
      // All failed. This method should have been overridden by the subclass
      // pulling it in. Whoever did this should be identified and shamed.
      throw new RuntimeException(e);
    }
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

  /**
   * Produce a very small string representation of the AST.
   *
   * This is used only to check if elements don't contain any text, and probably
   * should be done in a nicer way.
   *
   * Elements which are invisible should override this to return the empty
   * string.
   */
  public String toSmallString() {
    String out = getClass().getSimpleName();
    for (ASTNode child : getChildren()) {
      out += child.toSmallString();
    }
    return out;
  }

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof ASTNode)) {
      return false;
    }

    // Poor man's equality check: try "rendering" them both and compare the
    // results.
    return this.toStringTree().equals(((ASTNode) obj).toStringTree());
  }

  @Override
  public int hashCode() {
    return super.hashCode() + _children.size();
  }
}
