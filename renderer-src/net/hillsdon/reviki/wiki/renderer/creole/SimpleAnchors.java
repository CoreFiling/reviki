package net.hillsdon.reviki.wiki.renderer.creole;

/**
 * Specialisation of SimpleLinkHandler for anchors.
 */
public class SimpleAnchors extends SimpleLinkHandler {
  public static final String ANCHOR = "<a %sclass='%s' href='%s'>%s</a>";

  public SimpleAnchors(LinkResolutionContext context) {
    super(ANCHOR, context);
  }
}
