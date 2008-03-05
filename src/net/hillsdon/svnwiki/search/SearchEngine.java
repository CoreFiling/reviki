package net.hillsdon.svnwiki.search;

import java.io.IOException;
import java.util.Set;

public interface SearchEngine {

  /**
   * @param query Query.
   * @return Page names matching query, in rank order.
   * @throws IOException On error reading the search index. 
   * @throws QuerySyntaxException If the query is too broken to use. 
   */
  Set<String> search(String query) throws IOException, QuerySyntaxException;
  
}
