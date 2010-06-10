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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.hillsdon.fij.io.Lsof;
import net.hillsdon.reviki.search.SearchMatch;
import net.hillsdon.reviki.wiki.MarkupRenderer;
import net.hillsdon.reviki.wiki.RenderedPageFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Tests for {@link LuceneSearcher}.
 *
 * @author mth
 */
public class TestLuceneSearcher extends TestCase {

  private static final String WIKI_NAME = "wiki";
  private static final String WIKI_NAME2 = "wiki2";
  private static final String PAGE_THE_NAME = "TheName";
  private static final String PAGE_THE_NAME2 = "TheName2";
  private static final String PAGE_THE_NAME3 = "TheName3";
  private static final Set<SearchMatch> JUST_THE_PAGE = unmodifiableSet(singleton(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null)));
  private static final Set<SearchMatch> ALL_3 = unmodifiableSet(ImmutableSet.of(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null), new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME2, null), new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME3, null)));

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
  private File _dir2;
  private LuceneSearcher _searcher;
  private LuceneSearcher _searcher2;

  @Override
  protected void setUp() throws Exception {
    _dir = createTempDir();
    _dir2 = createTempDir();
    _searcher = new LuceneSearcher(WIKI_NAME, _dir, new File[]{_dir2}, new RenderedPageFactory(MarkupRenderer.AS_IS));
    _searcher2 = new LuceneSearcher(WIKI_NAME2, _dir2, new File[]{_dir}, new RenderedPageFactory(MarkupRenderer.AS_IS));
  }

  @Override
  protected void tearDown() throws Exception {
    cleanupTempDir(_dir);
    cleanupTempDir(_dir2);
  }
  
  protected void cleanupTempDir(File tmpDir) {
    // Nothing in tmpDir should be open.  Note this only works on Linux-like
    // machines at the moment (silently passing on others).
    for (File file : Lsof.lsof()) {
      final String dir = tmpDir.getAbsolutePath() + File.separator;
      final String filePath = file.getAbsolutePath();
      if (filePath.startsWith(dir)) {
        fail(file.toString() + " should be closed!");
      }
    }
    recursivelyDelete(tmpDir);
  }

  public void testRepeatedAddsForSamePathReplace() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true, false));
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the something else");
    assertEquals(emptySet(), _searcher.search("content", true, false));
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true, false));
  }

  public void testFindsByPath() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search(PAGE_THE_NAME, true, false));
    assertEquals(JUST_THE_PAGE, _searcher.search("path:The*", false, false));
  }

  public void testFindsCaseInsensitivelyByPath() throws Exception {
    try {
      assertEquals(JUST_THE_PAGE, _searcher.search(PAGE_THE_NAME.toLowerCase(Locale.US), true, false));
      throw new Error("Fixed bug!");
    }
    catch (AssertionFailedError bug) {
    }
  }

  public void testCaseInsensitiveLowerFindsMixed() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "The Content");
    assertEquals(JUST_THE_PAGE, _searcher.search("content", true, false));
  }

  public void testCaseInsensitiveMixedFindsLower() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("Content", true, false));
  }

  // Interestingly these fail while the others pass... when upgrading to Lucene 2.3.0.
  public void testMoreInterestingWords() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "cabbage patch");
    assertEquals(JUST_THE_PAGE, _searcher.search("cabbage", false, false));
    assertEquals(JUST_THE_PAGE, _searcher.search("patch", false, false));

    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "fruit flies");
    assertEquals(JUST_THE_PAGE, _searcher.search("fruit", false, false));
    assertEquals(JUST_THE_PAGE, _searcher.search("flies", false, false));
  }

  public void testFindsByTokenizedPath() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("name", true, false));
  }

  // FIXME: This doesn't actually test anything interesting, just added for the tearDown check.
  public void testIncomingOutgoingLinks() throws Exception {
    assertEquals(Collections.emptySet(), _searcher.incomingLinks(PAGE_THE_NAME));
    assertEquals(Collections.emptySet(), _searcher.outgoingLinks(PAGE_THE_NAME));
  }

  /**
   * It isn't a valid query but its daft to choke on it.
   */
  public void testTrimToEmptyStringNoResults() throws Exception {
    assertEquals(Collections.emptySet(), _searcher.search("  ", true, false));
  }

  public void testFindLowerPath() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("thename", false, false));
  }

  public void testFindPartialLowerPathCaseInsensitive() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("ThenA", false, false));
  }

  public void testFieldBasedQueryWithQuotes() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "the content");
    assertEquals(JUST_THE_PAGE, _searcher.search("path:\"TheName\"", false, false));
  }

  public void testAndByDefault() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "some content");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME2, -1, "some");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME3, -1, "content");
    assertEquals(JUST_THE_PAGE, _searcher.search("some content", false, false));
  }

  public void testOr() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "some content");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME2, -1, "some");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME3, -1, "content");
    assertEquals(ALL_3, _searcher.search("some OR content", false, false));
  }

  public void testLowercaseOrIsNotKeyword() throws Exception {
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "some content");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME2, -1, "some");
    _searcher.index(WIKI_NAME, PAGE_THE_NAME3, -1, "content");
    assertEquals(JUST_THE_PAGE, _searcher.search("some or content", false, false));
  }
  
  public void testMultiWiki() throws Exception {
    Set<SearchMatch> expected = unmodifiableSet(ImmutableSet.of(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null), new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME2, null)));
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "some content");
    _searcher2.index(WIKI_NAME2, PAGE_THE_NAME2, -1, "some other content");
    assertEquals(expected, _searcher.search("some or content", false, false));
    assertEquals(expected, _searcher2.search("some or content", false, false));
  }
  
  public void testMultiWikiOrder() throws Exception {
    Set<SearchMatch> expected = unmodifiableSet(ImmutableSet.of(new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME, null), new SearchMatch(true, WIKI_NAME, PAGE_THE_NAME2, null)));
    _searcher.index(WIKI_NAME, PAGE_THE_NAME, -1, "some content");
    _searcher2.index(WIKI_NAME2, PAGE_THE_NAME2, -1, "some other content");
    assertEquals(WIKI_NAME, _searcher.search("some or content", false, false).iterator().next().getWiki());
    assertEquals(WIKI_NAME2, _searcher2.search("some or content", false, false).iterator().next().getWiki());
  }
}
