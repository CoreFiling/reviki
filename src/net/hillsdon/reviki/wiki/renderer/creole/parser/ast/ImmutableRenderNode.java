package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import net.hillsdon.reviki.wiki.renderer.creole.RenderNode;

public abstract class ImmutableRenderNode implements RenderNode {
  public RenderNode addChildren(RenderNode... nodes) {
    return this;
  }
}
