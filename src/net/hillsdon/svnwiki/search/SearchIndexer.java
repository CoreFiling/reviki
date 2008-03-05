package net.hillsdon.svnwiki.search;

import java.io.IOException;


/**
 * Indexes page changes.
 * 
 * @author mth
 */
public interface SearchIndexer {

  void index(String path, String content) throws IOException;
  
}
