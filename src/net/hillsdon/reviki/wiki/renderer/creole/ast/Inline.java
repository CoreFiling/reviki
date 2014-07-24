package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class Inline extends ContainerNode<ASTNode> {
  public Inline(final List<ASTNode> chunks) {
    super(chunks);
  }
}
