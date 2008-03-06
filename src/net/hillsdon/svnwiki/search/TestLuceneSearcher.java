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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;
import net.hillsdon.svnwiki.wiki.MarkupRenderer;
import net.hillsdon.svnwiki.wiki.RenderedPageFactory;

/**
 * Tests for {@link LuceneSearcher}.
 * 
 * @author mth
 */
public class TestLuceneSearcher extends TestCase {

  private static final String PAGE_NAME = "TheName";
  private static final Set<SearchMatch> JUST_THE_PAGE = unmodifiableSet(singleton(new SearchMatch(PAGE_NAME, null)));
  
  private static File createTempDir() throws IOException {
    File file = File.createTempFile("testDir", "");
    assertTrue(file.delete());
    assertTrue(file.mkdir());
    return file;
  }
  
  private static void recursivelyDelete(final File dir) {
    File[] contents = dir.listFiles();
    for (File f : contents) {
      if (f.isFile()) {
        assertTrue(f.delete());
      }
      else {
        recursivelyDelete(f);
      }
    }
    assertTrue(dir.delete());
  }

  private File _dir;
  private LuceneSearcher _searcher;

  @Override
  protected void setUp() throws Exception {
    _dir = createTempDir();
    _searcher = new LuceneSearcher(_dir, new RenderedPageFactory(MarkupRenderer.AS_IS));
  }

  @Override
  protected void tearDown() throws Exception {
    recursivelyDelete(_dir);
  }

  public void testRepeatedAddsForSamePathReplace() throws Exception {
    _searcher.index(PAGE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true));
    _searcher.index(PAGE_NAME, -1, "the something else");
    assertEquals(emptySet(), _searcher.search("content", true));
    _searcher.index(PAGE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true));
  }
  
  public void testFindsByPath() throws Exception {
    _searcher.index(PAGE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search(PAGE_NAME, true));
    assertEquals(JUST_THE_PAGE, _searcher.search("path:The*", false));
  }
  
  public void testCaseInsensitiveLowerFindsMixed() throws Exception {
    _searcher.index(PAGE_NAME, -1, "The Content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true));
  }

  public void testCaseInsensitiveMixedFindsLower() throws Exception {
    _searcher.index(PAGE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("Content", true));
  }

  // Interestingly these fail while the others pass... when upgrading to Lucene 2.3.0.
  public void testMoreInterestingWords() throws Exception {
    _searcher.index(PAGE_NAME, -1, "cabbage patch");
    assertEquals(JUST_THE_PAGE, _searcher.search("cabbage", false));
    assertEquals(JUST_THE_PAGE, _searcher.search("patch", false));
    
    _searcher.index(PAGE_NAME, -1, "fruit flies");
    assertEquals(JUST_THE_PAGE, _searcher.search("fruit", false));
    assertEquals(JUST_THE_PAGE, _searcher.search("flies", false));
  }
  
  public void testFindsByTokenizedPath() throws Exception {
    _searcher.index(PAGE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("name", true));
  }
  
  /**
   * It isn't a valid query but its daft to choke on it.
   */
  public void testTrimToEmptyStringNoResults() throws Exception {
    assertEquals(Collections.emptySet(), _searcher.search("  ", true));
  }
  
}
