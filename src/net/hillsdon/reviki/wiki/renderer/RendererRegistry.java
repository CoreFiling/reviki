package net.hillsdon.reviki.wiki.renderer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.hillsdon.reviki.wiki.MarkupRenderer;

/**
 * A class which can provide renderers based on the desired result type.
 *
 * @author msw
 */
public class RendererRegistry {
  /** The default (HTML) renderer. */
  private final HtmlRenderer _default;

  /** Renderers which produce a stream of output. */
  private final Map<String, MarkupRenderer<InputStream>> _renderers;

  /**
   * @param html The default HTML renderer.
   */
  public RendererRegistry(final HtmlRenderer html) {
    _default = html;
    _renderers = new HashMap<String, MarkupRenderer<InputStream>>();
  }

  /** Return the default (HTML) renderer. */
  public HtmlRenderer getDefaultRenderer() {
    return _default;
  }

  /** Add a new stream renderer. */
  public void addRenderer(final String ctype, final MarkupRenderer<InputStream> renderer) {
    _renderers.put(ctype, renderer);
  }

  /** Check if we have a stream renderer for the desired ctype. */
  public boolean hasRenderer(final String ctype) {
    return _renderers.containsKey(ctype);
  }

  /** Get the stream renderer for this ctype. */
  public MarkupRenderer<InputStream> getRenderer(final String ctype) {
    return _renderers.get(ctype);
  }
}
