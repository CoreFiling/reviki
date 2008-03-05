package net.hillsdon.svnwiki.search;

import java.io.IOException;
import java.util.Set;

/**
 * Searches the wiki.
 * 
 * @author mth
 */
public interface SearchEngine {

  /**
   * @param query Query.
   * @return Matches for the query, in rank order.
   * @throws IOException On error reading the search index. 
   * @throws QuerySyntaxException If the query is too broken to use. 
   */
  Set<SearchMatch> search(String query) throws IOException, QuerySyntaxException;
  
}
