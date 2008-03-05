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
package net.hillsdon.svnwiki.wiki;

import java.io.IOException;
import java.util.Set;

import net.hillsdon.svnwiki.vc.PageStoreException;

/**
 * Allows navigation of page relationships.
 * 
 * The implementation is pretty naive though and it is missing a pretty
 * fundamental feature - we ought to be able to get the outgoing
 * links for a page too.  At the moment we don't index this information.
 * 
 * If our indexing was based off a rendered DOM rather than the raw wiki
 * markup then we'd have access to this information.  We could then add
 * a field to the Lucene document that held the outgoing links.  Orphaned
 * pages would then be cheap (if we can search for empty lucene fields).
 * 
 * @author mth
 */
public interface WikiGraph {

  Set<String> getIsolatedPages() throws IOException, PageStoreException;
  
  Set<String> getBacklinks(String page) throws IOException, PageStoreException;
  
}
