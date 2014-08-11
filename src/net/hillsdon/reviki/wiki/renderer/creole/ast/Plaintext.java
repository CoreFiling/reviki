package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class Plaintext extends TextNode {
  public Plaintext(final String contents) {
    super(contents, true);
  }
}
