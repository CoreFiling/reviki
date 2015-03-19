package net.hillsdon.reviki.wiki.renderer.creole.ast;

public class InlineNowiki extends TextNode implements BlockableNode<Nowiki> {

  public InlineNowiki(final String contents) {
    super(contents, true);
  }

  public Nowiki toBlock() {
    return new Nowiki(getText());
  }
}
