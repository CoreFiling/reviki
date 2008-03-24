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
package net.hillsdon.reviki.search.impl;

import java.io.IOException;

import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.vc.InterveningCommitException;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.impl.SimpleDelegatingPageStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Intercepts page edits in order to update the search index.
 * 
 * @author mth
 */
public class SearchIndexPopulatingPageStore extends SimpleDelegatingPageStore {

  private static final Log LOG = LogFactory.getLog(SearchIndexPopulatingPageStore.class);

  private final SearchEngine _indexer;

  public SearchIndexPopulatingPageStore(final SearchEngine indexer, final PageStore delegate) {
    super(delegate);
    _indexer = indexer;
  }

  @Override
  public long set(final PageReference ref, final String lockToken, final long baseRevision, final String content, final String commitMessage) throws InterveningCommitException, PageStoreException {
    long newRevision = super.set(ref, lockToken, baseRevision, content, commitMessage);
    try {
      _indexer.index(ref.getPath(), newRevision, content);
    }
    catch (IOException e) {
       LOG.error("Error adding to search index, skipping page: " + ref, e);
    }
    return newRevision;
  }
  
}
