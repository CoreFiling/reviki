package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.io.Writer;

public interface MarkupRenderer {
  
  public void render(String in, Writer out) throws IOException;
    
}
