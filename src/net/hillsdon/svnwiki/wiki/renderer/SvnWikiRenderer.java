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
import net.hillsdon.svnwiki.wiki.renderer.creole.CreoleImageNode;
import net.hillsdon.svnwiki.wiki.renderer.creole.CreoleRenderer;
import net.hillsdon.svnwiki.wiki.renderer.creole.RenderNode;

public class SvnWikiRenderer implements MarkupRenderer {

  private final CreoleRenderer _creole;
  
  public SvnWikiRenderer(final Configuration configuration, final InternalLinker internalLinker) {
    final SvnWikiLinkPartHandler linkHandler = new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.ANCHOR, internalLinker, configuration);
    final List<Macro> macros = new ArrayList<Macro>();
    _creole = new CreoleRenderer(
        new RenderNode[] {
            new UnescapedHtmlNode(true),
            new JavaSyntaxHighlightedNode(true),
        },
        new RenderNode[] {
            new JavaSyntaxHighlightedNode(false),
            new UnescapedHtmlNode(false),
            new CreoleImageNode(new SvnWikiLinkPartHandler(SvnWikiLinkPartHandler.IMAGE, internalLinker, configuration)),
            new CreoleLinkNode(linkHandler),
            new CustomWikiLinkNode(linkHandler),
            new MacroNode(macros),
        });
  }

  public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
    out.write(_creole.render(page, in));
  }

}
