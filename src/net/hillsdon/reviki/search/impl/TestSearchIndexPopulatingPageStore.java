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

import java.util.Collections;

import junit.framework.TestCase;
import net.hillsdon.reviki.search.SearchEngine;
import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.vc.PageStore;
import net.hillsdon.reviki.vc.impl.PageInfoImpl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestSearchIndexPopulatingPageStore extends TestCase {

  private PageStore _mockedDelegate;
  private SearchEngine _mockedSearchEngine;
  private SearchIndexPopulatingPageStore _populatingPageStore;

  @Override
  protected void setUp() throws Exception {
    _mockedDelegate = createMock(PageStore.class);
    _mockedSearchEngine = createMock(SearchEngine.class);
    _populatingPageStore = new SearchIndexPopulatingPageStore(_mockedSearchEngine, _mockedDelegate);
  }

  public void testDelegatesThenIndexesAgainstNewRevisionNumber() throws Exception {
    final String lockToken = "";
    final long baseRevision = 12;
    final String content = "New content";
    final String commitMessage = "Did something";
    final PageInfo page = new PageInfoImpl(null, "SomePage", content, Collections.<String, String>emptyMap());

    final long newRevision = 15;

    expect(_mockedDelegate.set(page, lockToken, baseRevision, commitMessage)).andReturn(newRevision );
    expect(_mockedSearchEngine.isIndexBeingBuilt()).andReturn(false);
    _mockedSearchEngine.index(page, false);
    _mockedSearchEngine.rememberHighestIndexedRevision(newRevision);
    replay(_mockedDelegate, _mockedSearchEngine);
    _populatingPageStore.set(page, lockToken, baseRevision, commitMessage);
    verify(_mockedDelegate, _mockedSearchEngine);
  }

}
