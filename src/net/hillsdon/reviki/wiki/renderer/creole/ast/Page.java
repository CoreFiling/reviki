package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Page extends ContainerNode<ASTNode> {
  public Page(final List<ASTNode> blocks) {
    super(blocks);
  }
}
