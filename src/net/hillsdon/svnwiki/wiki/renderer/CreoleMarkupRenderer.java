package net.hillsdon.svnwiki.wiki.renderer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.InternalLinker;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;

public class CreoleMarkupRenderer implements MarkupRenderer {

  private final CreoleRenderer _creole;
  
  public CreoleMarkupRenderer(final Configuration configuration, final InternalLinker internalLinker) {
    final List<Macro> macros = new ArrayList<Macro>();
    macros.add(new LinkMacro());
    macros.add(new AttachedMacro("attached", "<a href='%s'>%s</a>"));
    macros.add(new AttachedMacro("image", "<img src='%s' />"));
    
    _creole = new CreoleRenderer(
        new RenderNode[] {
            new CustomWikiLinkNode(internalLinker, configuration),
            new UnescapedHtmlNode(),
            new MacroNode(macros)
        });
  }

  public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
    out.write(_creole.render(page, in));
  }

}
