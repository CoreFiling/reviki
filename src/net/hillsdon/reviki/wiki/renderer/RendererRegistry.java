package net.hillsdon.reviki.wiki.renderer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.hillsdon.reviki.web.common.ViewTypeConstants;
import net.hillsdon.reviki.wiki.MarkupRenderer;

/**
 * A class which can provide renderers based on the desired result type.
 *
 * @author msw
 */
public class RendererRegistry {
  /** Renderers which can put content directly into a HTMl page. */
  private final Map<String, MarkupRenderer<String>> _pageOutputRenderers;

  /** Renderers which produce a stream of output. */
  private final Map<String, MarkupRenderer<InputStream>> _streamOutputRenderers;

  /**
   * @param html The default renderer.
   */
  public RendererRegistry(final HtmlRenderer html) {
    _pageOutputRenderers = new HashMap<String, MarkupRenderer<String>>();
    _streamOutputRenderers = new HashMap<String, MarkupRenderer<InputStream>>();

    addPageOutputRenderer(ViewTypeConstants.CTYPE_DEFAULT, html);
  }

  /** Add a new string renderer. */
  public void addPageOutputRenderer(final String ctype, final MarkupRenderer<String> renderer) {
    _pageOutputRenderers.put(ctype, renderer);
  }

  /** Add a new stream renderer. */
  public void addStreamOutputRenderer(final String ctype, final MarkupRenderer<InputStream> renderer) {
    _streamOutputRenderers.put(ctype, renderer);
  }

  /** Check if we have a string renderer for the desired ctype. */
  public boolean hasPageOutputRenderer(final String ctype) {
    return _pageOutputRenderers.containsKey(ctype);
  }

  /** Check if we have a stream renderer for the desired ctype. */
  public boolean hasStreamOutputRenderer(final String ctype) {
    return _streamOutputRenderers.containsKey(ctype);
  }

  /** Get the string renderer for this ctype. */
  public MarkupRenderer<String> getPageOutputRenderer(final String ctype) {
    return _pageOutputRenderers.get(ctype);
  }

  /** Get the stream renderer for this ctype. */
  public MarkupRenderer<InputStream> getStreamOutputRenderer(final String ctype) {
    return _streamOutputRenderers.get(ctype);
  }
}
