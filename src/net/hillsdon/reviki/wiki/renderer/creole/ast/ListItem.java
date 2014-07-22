package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.List;

public class ListItem extends ASTNode {
  public ListItem(ASTNode body, List<ASTNode> sublists) {
    super("li", body, sublists);
  }
  
  public ListItem(ASTNode body) {
    super("li", body);
  }
}
