package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class UnorderedList extends TaggedNode {
  public UnorderedList(final List<ASTNode> children) {
    super("ul", children);
  }
}
