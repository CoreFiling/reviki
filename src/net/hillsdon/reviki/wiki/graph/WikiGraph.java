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
package net.hillsdon.reviki.wiki.graph;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.vc.PageStoreException;

/**
 * Allows navigation of page relationships.
 * 
 * @author mth
 */
public interface WikiGraph {

  /**
   * @return Pages that have no incoming links (excluding themselves).
   * @throws IOException On error reading an index.
   * @throws PageStoreException On error reading wiki data.
   */
  Set<String> isolatedPages() throws IOException, PageStoreException;
  
  /**
   * @return Incoming links for the page (excluding the page itself).
   * @throws IOException On error reading an index.
   * @throws PageStoreException On error reading wiki data.
   */
  Set<SearchMatch> incomingLinks(String page) throws IOException, PageStoreException;
  
  /**
   * @return Outgoing links for the page (excluding the page itself).
   * @throws IOException On error reading an index.
   * @throws PageStoreException On error reading wiki data.
   */
  Set<SearchMatch> outgoingLinks(String page) throws IOException, PageStoreException;

}
