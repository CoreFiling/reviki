package net.hillsdon.reviki.wiki.renderer.creole.parser.ast;

import net.hillsdon.fij.text.Escape;

public class Plaintext extends ASTNode {
  protected String contents;

  public Plaintext(String contents) {
    super("", null, null);
    this.contents = contents;
  }

  public String toXHTML() {
    return Escape.html(contents);
  }
}
