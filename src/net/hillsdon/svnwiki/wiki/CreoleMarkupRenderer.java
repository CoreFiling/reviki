package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.configuration.Configuration;
import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.renderer.CreoleRenderer;
import net.hillsdon.svnwiki.wiki.renderer.CustomWikiLinkNode;

public class CreoleMarkupRenderer implements MarkupRenderer {

  private final CreoleRenderer _creole;
  
  public CreoleMarkupRenderer(final Configuration configuration, final InternalLinker internalLinker) {
    _creole = new CreoleRenderer(new CustomWikiLinkNode(internalLinker, configuration));
  }

  public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
    out.write(_creole.render(in));
  }

}
