package net.hillsdon.reviki.wiki.renderer.creole;

/**
 * Specialisation of SimpleLinkHandler for anchors.
 */
public class SimpleAnchors extends SimpleLinkHandler {
  public SimpleAnchors(LinkResolutionContext context) {
    super(SimpleLinkHandler.ANCHOR, context);
  }
}
