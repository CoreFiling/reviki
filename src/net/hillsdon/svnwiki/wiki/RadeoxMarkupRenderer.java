package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;


public class RadeoxMarkupRenderer implements MarkupRenderer {
  
  private RenderEngine _engine = new BaseRenderEngine();

  public void render(String in, Writer out) throws IOException {
    RenderContext context = new BaseRenderContext();
    _engine.render(out, in, context);
  }
  
}
