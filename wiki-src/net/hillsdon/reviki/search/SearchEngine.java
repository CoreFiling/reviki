/**
 * Copyright 2008 Matthew Hillsdon
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
package net.hillsdon.reviki.search;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStoreException;

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
   * @param provideExtracts true if extracts from the matching text should be provided in the returned matches (slower).
   * @param singleWiki true if the search should be restricted to the current wiki.
   * @return Matches for the query, in rank order.
   * @throws IOException On error reading the search index.
   * @throws QuerySyntaxException If the query is too broken to use.
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  Set<SearchMatch> search(String query, boolean provideExtracts, boolean singleWiki) throws IOException, QuerySyntaxException, PageStoreException;

  /**
   * @param page A page.
   * @return Outgoing links from that page, excluding that page.
   * @throws IOException On error reading the search index.
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  Set<String> outgoingLinks(String page) throws IOException, PageStoreException;

  /**
   * @param page A page.
   * @return Incoming links to that page, excluding that page.
   * @throws IOException On error reading the search index.
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  Set<String> incomingLinks(String page) throws IOException, PageStoreException;

  /**
   * Indexes page change.
   *
   * @param page Page to be indexed.
   * @param buildingIndex Flag indicating whether we are building index.
   * @throws IOException On error writing to the search index.
   * @throws PageStoreException If an error occurs reading wiki-data.
   */
  void index(PageInfo page, boolean buildingIndex) throws IOException, PageStoreException;

  /**
   * @return The highest revision number indexed (as passed to index). Returns -1 if the index has failed to built.
   * @throws IOException On error reading from the search index.
   */
  long getHighestIndexedRevision() throws IOException;

  /**
   * @param revision The highest revision number indexed.
   * @throws IOException On error reading from the search index.
   */
  void rememberHighestIndexedRevision(long revision) throws IOException;

  /**
   * @return True if the index is currently being built, false otherwise.
   * @throws IOException On error reading from the search index.
   */
  boolean isIndexBeingBuilt() throws IOException;

  /**
   * @param revision Boolean indicating whether the index is being built.
   * @throws IOException On error reading from the search index.
   */
  void setIndexBeingBuilt(boolean buildingIndex) throws IOException;

  /**
   * @param wiki Wiki name.
   * @param path Page.
   * @param buildingIndex Flag indicating whether we are building index.
   * @throws IOException On error writing to the search index.
   */
  void delete(String wiki, String path, boolean buildingIndex) throws IOException;

  /**
   * @param in A string.
   * @return A quoted version that escapes any characters that have special significance in the search syntax.
   */
  String escape(String in);

}
