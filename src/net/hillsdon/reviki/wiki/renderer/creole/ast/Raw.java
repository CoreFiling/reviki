package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Raw extends ASTNode {
  protected String contents;

  public Raw(String contents) {
    super("", null, null);
    this.contents = contents;
  }

  public String toXHTML() {
    return contents;
  }
}
