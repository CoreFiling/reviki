package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageStoreException;

public interface MarkupRenderer {
  
  public void render(String in, Writer out) throws IOException, PageStoreException;
    
}
