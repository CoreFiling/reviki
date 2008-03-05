package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStore;
import net.hillsdon.svnwiki.vc.PageStoreException;

import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.filter.LinkTestFilter;
import org.radeox.macro.MacroRepository;

public class RadeoxMarkupRenderer implements MarkupRenderer {
  
  private final RenderEngine _engine;
  private final Configuration _configuration;

  public RadeoxMarkupRenderer(final Configuration configuration, final PageStore store) {
    AttachedFileMacro attached = new AttachedFileMacro();
    AttachedImageMacro image = new AttachedImageMacro();
    MacroRepository.getInstance().put(attached.getName(), attached);
    MacroRepository.getInstance().put(image.getName(), image);
    
    _configuration = configuration;
    _engine = new SvnWikiRenderEngine(store);
    _engine.getInitialRenderContext().setRenderEngine(_engine);
    _engine.getInitialRenderContext().getFilterPipe().deactivateFilter(LinkTestFilter.class.getName());
    _engine.getInitialRenderContext().getFilterPipe().addFilter(new CustomWikiLinkFilter());
  }
  
  public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
    RenderContext context = new BaseRenderContext();
    context.set("page", page);
    context.set(CustomWikiLinkFilter.INTERWIKI_LINKER_CONTEXT_KEY, _configuration.getInterWikiLinker());
    context.setRenderEngine(_engine);
    _engine.render(out, in, context);
  }
  
}
