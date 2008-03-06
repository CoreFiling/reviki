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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.hillsdon.reviki.vc.ChangeInfo;
import net.hillsdon.reviki.vc.ChangeSubscriber;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageReference;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.PageStoreException;
import net.hillsdon.reviki.vc.StoreKind;

/**
 * Notifies the search engine of page changes immediately after they happen.
 * 
 * Important else we get backlinks wrong for new pages (as we only check
 * for commits at the beginning of each request).
 * 
 * @author mth
 */
public class ExternalCommitAwareSearchEngine implements SearchEngine, ChangeSubscriber {

  private PageStore _store;
  private final SearchEngine _delegate;

  public ExternalCommitAwareSearchEngine(final SearchEngine delegate) {
    _delegate = delegate;
  }
  
  /**
   * Setter to avoid circularity.  If set we'll notice external commits otherwise just delegate.
   */
  public void setPageStore(final PageStore store) {
    _store = store;
  }
  
  public void index(final String path, final long revision, final String content) throws IOException, PageStoreException {
    _delegate.index(path, revision, content);
  }

  public Set<SearchMatch> search(final String query, final boolean provideExtracts) throws IOException, QuerySyntaxException, PageStoreException {
    return _delegate.search(query, provideExtracts);
  }

  public long getHighestSyncedRevision() throws IOException {
    return _delegate.getHighestIndexedRevision();
  }
  
  public synchronized void handleChanges(final long upto, final List<ChangeInfo> chronological) throws PageStoreException, IOException {
    // We're going to work from head for the indexing so collapse edits down to page names.
    final Set<PageReference> minimized = new LinkedHashSet<PageReference>();
    for (ChangeInfo change : chronological) {
      if (change.getKind() == StoreKind.PAGE) {
        minimized.add(new PageReference(change.getPage()));
      }
    }
    for (PageReference page : minimized) {
      PageInfo info = _store.get(page, -1);
      // Note we pass 'upto' as the revision here.  At the moment we get
      // back the revision of deleted pages as -2 which isn't such a good
      // thing to set our 'highest indexed revision' to...
      if (info.isNew()) {
        _delegate.delete(info.getPath(), upto);
      }
      else {
        _delegate.index(info.getPath(), upto, info.getContent());
      }
    }
  }

  public long getHighestIndexedRevision() throws IOException {
    return _delegate.getHighestIndexedRevision();
  }

  public void delete(final String path, final long revision) throws IOException {
    _delegate.delete(path, revision);
  }

  public String escape(final String in) {
    return _delegate.escape(in);
  }

  public Set<String> incomingLinks(final String page) throws IOException, PageStoreException {
    return _delegate.incomingLinks(page);
  }

  public Set<String> outgoingLinks(final String page) throws IOException, PageStoreException {
    return _delegate.outgoingLinks(page);
  }
  
}
