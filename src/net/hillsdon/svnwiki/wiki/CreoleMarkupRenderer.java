package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;
import net.hillsdon.svnwiki.wiki.renderer.CreoleRenderer;

public class CreoleMarkupRenderer implements MarkupRenderer {

  private CreoleRenderer _creole = new CreoleRenderer();

  public void render(final PageReference page, final String in, final Writer out) throws IOException, PageStoreException {
    out.write(_creole.render(in));
  }

}
