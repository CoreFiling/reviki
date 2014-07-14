package net.hillsdon.reviki.wiki.renderer.creole.ast;

import java.util.Arrays;

public class ListItem extends ASTNode {
  public ListItem(ASTNode body, ASTNode sublist) {
    super("li", body, Arrays.asList(new ASTNode[] {sublist}));
  }
  
  public ListItem(ASTNode body) {
    super("li", body);
  }
}
