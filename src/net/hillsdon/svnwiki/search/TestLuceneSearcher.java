package net.hillsdon.svnwiki.search;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import junit.framework.TestCase;

public class TestLuceneSearcher extends TestCase {
  
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
    _searcher = new LuceneSearcher(_dir);
  }

  @Override
  protected void tearDown() throws Exception {
    recursivelyDelete(_dir);
  }

  public void testRepeatedAddsForSamePathReplace() throws Exception {
    final String path = "ThePath";
    _searcher.index(path, "the content");
    assertEquals(singleton(new SearchMatch(path, null)), _searcher.search("content"));
    _searcher.index(path, "the something else");
    assertEquals(emptySet(), _searcher.search("content"));
    _searcher.index(path, "the content");
    assertEquals(singleton(new SearchMatch(path, null)), _searcher.search("content"));
  }
  
  public void testFindsByPath() throws Exception {
    final String path = "ThePath";
    _searcher.index(path, "the content");
    assertEquals(singleton(new SearchMatch(path, null)), _searcher.search(path));
  }
  
  public void testFindsByTokenizedPath() throws Exception {
    final String path = "ThePath";
    _searcher.index(path, "the content");
    assertEquals(singleton(new SearchMatch(path, null)), _searcher.search("path"));
  }
  
  /**
   * It isn't a valid query but its daft to choke on it.
   */
  public void testTrimToEmptyStringNoResults() throws Exception {
    assertEquals(Collections.emptySet(), _searcher.search("  "));
  }
  
}
