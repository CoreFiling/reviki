package net.hillsdon.svnwiki.search;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Searches the wiki.
 * 
 * @author mth
 */
public interface SearchEngine {

  /**
   * Search for pages.
   * 
   * @param query Query.
   * @return Matches for the query, in rank order.
   * @throws IOException On error reading the search index. 
   * @throws QuerySyntaxException If the query is too broken to use. 
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  Set<SearchMatch> search(String query) throws IOException, QuerySyntaxException, PageStoreException;

  /**
   * Indexes page change.
   * 
   * @param path Page.
   * @param revision The revision number of the new content.
   * @param content New content.
   * @throws IOException On error writing to the search index. 
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  void index(String path, long revision, String content) throws IOException, PageStoreException;

  /**
   * @return The highest revision number indexed (as passed to index).
   * @throws IOException On error reading from the search index. 
   */
  long getHighestIndexedRevision() throws IOException;

  /**
   * @param path Page.
   * @param revision Revision at which we noticed its passing.
   * @throws IOException 
   */
  void delete(String path, long revision) throws IOException;

  /**
   * @param in A string.
   * @return A quoted version that escapes any characters that have special significance in the search syntax.
   */
  String escape(String in);
  
}
