package net.hillsdon.reviki.wiki.renderer.creole;

/**
 * Specialisation of SimpleLinkHandler for images.
 */
public class SimpleImages extends SimpleLinkHandler {
  public SimpleImages(LinkResolutionContext context) {
    super(SimpleLinkHandler.IMAGE, context);
  }
}
