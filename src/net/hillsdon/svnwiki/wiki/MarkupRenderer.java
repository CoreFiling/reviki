package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

import net.hillsdon.svnwiki.vc.PageReference;
import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Interface for something that renders wiki markup.
 * 
 * @author mth
 */
public interface MarkupRenderer {
  
  void render(PageReference page, String in, Writer out) throws IOException, PageStoreException;
    
}
