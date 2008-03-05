package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.StringWriter;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;

public class RenderedPageFactory {

  private final MarkupRenderer _renderer;

  public RenderedPageFactory(final MarkupRenderer renderer) {
    _renderer = renderer;
  }
  
  public RenderedPage create(final String pageName, final String content) throws IOException, PageStoreException {
    StringWriter rendered = new StringWriter();
    _renderer.render(new PageReference(pageName), content, rendered);
    return new RenderedPage(pageName, rendered.toString());
  }
  
}
