package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Allows navigation of page relationships.
 * 
 * The implementation is pretty naive though.
 * 
 * @author mth
 */
public interface WikiGraph {

  Set<String> getIsolatedPages() throws IOException, PageStoreException;
  
  Set<String> getBacklinks(String page) throws IOException, PageStoreException;
  
}
