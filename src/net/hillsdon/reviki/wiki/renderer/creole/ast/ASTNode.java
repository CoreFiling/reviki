package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /** Whether this node is block-level or not. */
  protected boolean _isBlock = false;

  /** Whether this node can contain block-level nodes or not. */
  protected boolean _canContainBlock = false;

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
  public final String toXHTML() {
    return toXHTML(new HashMap<String, List<String>>());
  }

  /**
   * Render the node with the given directives (and their arguments) enabled.
   *
   * enabledDirectives MUST be mutable.
   * This method MAY mutate enabledDirectives.
   */
  public String toXHTML(Map<String, List<String>> enabledDirectives) {
    String out = "";

    for (ASTNode node : getChildren()) {
      out += node.toXHTML(enabledDirectives);
    }

    return out;
  }

  /**
   * Expand macros contained within this node and its children, returning the
   * modified node. If no macros were expanded, `this` is returned.
   *
   * @param macros The list of macros
   * @return A node, with macros expanded.
   */
  public final ASTNode expandMacros(final Supplier<List<Macro>> macros) {
    return expandMacrosInt(macros).get(0);
  }

  /**
   * Expand macros, where a macro may cause new nodes to come into existence.
   */
  protected List<ASTNode> expandMacrosInt(final Supplier<List<Macro>> macros) {
    // Expand all children
    boolean mutated = false;
    List<ASTNode> adoptees = new ArrayList<ASTNode>();
    List<ASTNode> expandedBits = new ArrayList<ASTNode>();

    for (ASTNode child : _children) {
      List<ASTNode> expanded = child.expandMacrosInt(macros);

      if (expanded.size() == 1 && expanded.get(0) == child) {
        // No mutation occurred
        adoptees.add(expanded.get(0));
      }
      else {
        for (ASTNode node : expanded) {
          if (canMergeWith(node)) {
            // An identical node of the same type as this one was created, but
            // we can merge the nodes! In this case, we need to extract the
            // useful information from the new node, and add it to the children
            // of this one.
            adoptees = mergeChildren(adoptees, node);
          }
          else if (node._isBlock && !_canContainBlock) {
            // A block node was created by macro expansion, BUT this node cannot
            // contain it! In this case, split the children around this block,
            // wrapping each part in the node, and return a list of chunks.
            if (!adoptees.isEmpty()) {
              expandedBits.add(conjure(adoptees));
              adoptees.clear();
            }

            expandedBits.add(node);
          }
          else {
            adoptees.add(node);
          }
        }

        mutated = true;
      }
    }

    // If no children were expanded, return `this`.
    if (!mutated) {
      return ImmutableList.of(this);
    }

    // If no new block-level elements have been introduced, we can directly
    // create a new node.
    if (expandedBits.isEmpty()) {
      return ImmutableList.of(conjure(adoptees));
    }

    // If all else fails, we return the list of expanded chunks so something
    // higher-up in the AST can figure out what to do.
    return expandedBits;
  }

  /**
   * This uses reflection to instantiate a new node, and so MUST be overridden
   * if the constructors are different.
   *
   * @return A new node of the same type, with the given children.
   */
  protected ASTNode conjure(final List<ASTNode> children) {
    // Prefer the single-node constructor if it's available and we only have
    // one child.
    if (children.size() == 1) {
      try {
        Constructor<? extends ASTNode> constructor = getClass().getDeclaredConstructor(ASTNode.class);
        return constructor.newInstance(children.get(0));
      }
      catch (Exception e) {
        // The single-node constructor might not be available, but the list
        // one might be, so just ignore this exception and continue.
      }
    }

    try {
      Constructor<? extends ASTNode> constructor = getClass().getDeclaredConstructor(List.class);
      return constructor.newInstance(children);
    }
    catch (Exception e) {
      // All failed. This method should have been overridden by the subclass
      // pulling it in. Whoever did this should be identified and shamed.
      throw new RuntimeException(e);
    }
  }

  /**
   * Determine if the target node can be merged into this one, meaningfully, as
   * a result of macro expansion introducing extraneous blocks.
   */
  protected boolean canMergeWith(ASTNode node) {
    return false;
  }

  /**
   * Merge the target node with the given children. The children list MAY be
   * mutated.
   *
   * This MUST be overridden if canMergeWith can be true!
   */
  protected List<ASTNode> mergeChildren(List<ASTNode> children, ASTNode node) {
    throw new UnsupportedOperationException("This needs to be overridden!");
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
