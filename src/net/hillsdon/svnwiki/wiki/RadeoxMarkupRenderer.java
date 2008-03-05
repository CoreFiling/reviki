package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageStore;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.filter.LinkTestFilter;

public class RadeoxMarkupRenderer implements MarkupRenderer {
  
  private RenderEngine _engine;

  public RadeoxMarkupRenderer(final PageStore store) {
    _engine = new SvnWikiRenderEngine(store);
    _engine.getInitialRenderContext().setRenderEngine(_engine);
    // This needs to be configurable...
    InterWikiLinker iwl = new InterWikiLinker();
    iwl.addWiki("smbug", "https://candide.corefiling.com/~bugs/show_bug.cgi?id=%s");
    
    _engine.getInitialRenderContext().getFilterPipe().deactivateFilter(LinkTestFilter.class.getName());
    _engine.getInitialRenderContext().getFilterPipe().addFilter(new CustomWikiLinkFilter(iwl));
  }
  
  public void render(final String in, final Writer out) throws IOException {
    RenderContext context = new BaseRenderContext();
    context.setRenderEngine(_engine);
    _engine.render(out, in, context);
  }
  
}
