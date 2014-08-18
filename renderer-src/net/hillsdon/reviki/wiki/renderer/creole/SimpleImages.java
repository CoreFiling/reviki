package net.hillsdon.reviki.wiki.renderer.creole;

/**
 * Specialisation of SimpleLinkHandler for images.
 */
public class SimpleImages extends SimpleLinkHandler {
  public static final String IMAGE = "<img %sclass='%s' src='%s' alt='%s' />";

  public SimpleImages(LinkResolutionContext context) {
    super(IMAGE, context);
  }
}
