package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class ListItem extends ASTNode {
  public ListItem(final ASTNode body, final List<ASTNode> sublists) {
    super("li", body, sublists);
  }
  
  public ListItem(final ASTNode body) {
    super("li", body);
  }
}
