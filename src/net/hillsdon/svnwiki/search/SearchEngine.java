/**
 * Copyright 2007 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   * @param provideExtracts TODO
   * @return Matches for the query, in rank order.
   * @throws IOException On error reading the search index. 
   * @throws QuerySyntaxException If the query is too broken to use. 
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  Set<SearchMatch> search(String query, boolean provideExtracts) throws IOException, QuerySyntaxException, PageStoreException;

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
